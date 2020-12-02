#!/bin/bash
#
# Copyright (c) 2020, 2020 IBM Corporation, Red Hat and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
###############################################################

# Common functions used in Kruize tests

curl_cmd=""


#
# Check error code from last command, exit on error
#

function check_err() {
        err=$?
        if [ ${err} -ne 0 ]; then
                echo "$*"
                exit -1
        fi
}

#
# Check if jq is installed
#

function check_prereq() {
        echo
        echo "Info: Checking prerequisites..."
        # check if jq exists
        if ! [ -x "$(command -v jq)" ]; then
                echo "Error: jq is not installed."
                exit 1
        fi
}

#
# Deploy kruize on the specified cluster 
# 
#

function deploy_kruize() {
        echo
        pushd $KRUIZE_REPO > /dev/null
	kruize_log="$RESULTS_DIR/deploy-kruize.log"

	if [ $cluster_type == "docker" ]; then
		cmd="./deploy.sh -ac $cluster_type -i ${KRUIZE_DOCKER_IMAGE} --timeout=1200 &"
	elif [ $cluster_type == "minikube" ]; then
		cmd="./deploy.sh -ac $cluster_type -i $KRUIZE_DOCKER_IMAGE -n $kruize_ns"
	elif [ $cluster_type == "openshift" ]; then
		openshift_url="https://api.$kurl:6443"
        	cmd="./deploy.sh -ac $cluster_type -i $KRUIZE_DOCKER_IMAGE -k ${openshift_url} -u ${user} -p ${password} -n ${kruize_ns}"
	elif [ $cluster_type == "icp" ]; then
		cmd="./deploy.sh -ac $cluster_type -i $KRUIZE_DOCKER_IMAGE -k ${kurl} -u ${user} -p ${password} -n ${kruize_ns}"
	fi

       	echo $cmd
	if [ $cluster_type == "docker" ]; then
		./deploy.sh -ac $cluster_type -i ${KRUIZE_DOCKER_IMAGE} --timeout=1200 > $kruize_log 2>&1 &
	else
	        $cmd | tee -a $kruize_log
	fi
	
        popd > /dev/null
        sleep 60

	if [ $cluster_type == "docker" ]; then
		if grep -q "Error" "$kruize_log"; then
			echo "Failed - Deploying kruize failed, check $kruize_log for details"
			exit -1
		fi
	fi

	if [ $cluster_type == "openshift" ]; then
		oc expose svc/kruize -n $kruize_ns
	fi
	form_curl_cmd
	echo "curl_cmd = $curl_cmd"
}

#
# Deploy acmeair application
#

function deploy_acmeair() {
        echo
        echo "Deploying acmeair app..."

	if [ $cluster_type == "docker" ]; then
		echo
		echo "  - name: \"acmeair-mono-app1\"" >> "$KRUIZE_REPO/manifests/docker/kruize-docker.yaml" 
		echo "Updated $KRUIZE_REPO/manifests/docker/kruize-docker.yaml..."
		cat $KRUIZE_REPO/manifests/docker/kruize-docker.yaml
		echo
        	pushd "$APP_REPO/acmeair/scripts" > /dev/null
	        ./acmeair-setup.sh
        	popd > /dev/null
	        sleep 30
	else 
		app_ns="default"

		cmd="kubectl"
		login_cmd="cloudctl"
		url=$kurl

		
		if [ $cluster_type == "icp" ]; then
			app_ns="cert-manager"
		fi

		if [[ $cluster_type == "icp" || $cluster_type == "openshift" ]]; then	
			if [ $cluster_type == "openshift" ]; then
				cmd="oc"
				login_cmd="oc"
				url="https://api.$kurl:6443"
				
			fi
			echo "$login_cmd $cmd $url $user $password $kruize_ns $app_ns $APP_REPO"
			$login_cmd login ${url} -u ${user} -p ${password} -n $kruize_ns 
		fi

		# Deploy mongo db
	       	$cmd create -f $APP_REPO/acmeair/manifests/mongo-db.yaml -n $app_ns

		# Deploy acmeair app
		$cmd create -f $APP_REPO/acmeair/manifests/acmeair.yaml -n $app_ns
		if [[ $cluster_type == "openshift" ]]; then
			# Create a route to the service by exposing it on openshift
        		$cmd expose svc/acmeair-service -n $app_ns
		fi
	fi
	echo "done"
}

#
# Run jmeter load for acmeair application
#

function run_acmeair_jmeter_load() {
        echo
        echo "Starting acmeair jmeter workload..."

	if [ $cluster_type == "openshift" ]; then
       		pushd "$APP_REPO/acmeair/tests" > /dev/null

		# Form the acmeair service name
	        OPENSHIFT_ACMEAIR_SERVICE="acmeair-service-default.apps.$kurl"
        	echo "openshift_acmeair_service=${OPENSHIFT_ACMEAIR_SERVICE}"

		# Invoke the jmeter load script
	        ./openshift-load.sh ${OPENSHIFT_ACMEAIR_SERVICE} 
	elif [[ $cluster_type == "icp" || $cluster_type == "minikube" ]]; then
        	pushd "$APP_REPO/acmeair/tests" > /dev/null
		SERVER=$(echo $kurl | awk -F[/:] '{print $4}')
	        echo "SERVER = $SERVER"

		# Invoke the jmeter load script
        	./icp-load.sh ${SERVER} 
	elif [ $cluster_type == "docker" ]; then
        	pushd "$APP_REPO/acmeair/scripts" > /dev/null
		MAX_LOOP=3
		./acmeair-load.sh $MAX_LOOP
	fi
	popd > /dev/null

}

#
# Update prometheus.yaml with the application details to scrape metrics for docker case
#

function update_prometheus_yaml() {
	num_instances=1
	# Update prometheus.yaml with petclinic job
	PROMETHEUS_YAML="$KRUIZE_REPO/manifests/docker/prometheus.yaml"
	port=8081
        for(( inst=0; inst<${num_instances}; inst++ ))
        do
		echo "- job_name: petclinic-app-$inst" >> $PROMETHEUS_YAML
		echo "  honor_timestamps: true" >> $PROMETHEUS_YAML
		echo "  scrape_interval: 2s" >> $PROMETHEUS_YAML
		echo "  scrape_timeout: 1s" >> $PROMETHEUS_YAML
		echo "  metrics_path: /manage/prometheus" >> $PROMETHEUS_YAML
		echo "  scheme: http" >> $PROMETHEUS_YAML
		echo "  static_configs:" >> $PROMETHEUS_YAML
		echo "  - targets:" >> $PROMETHEUS_YAML
		echo "      - petclinic-app-$inst:$port" >> $PROMETHEUS_YAML
		((port=port+1))

	done
	echo
	echo "Updated Prometheus yaml..."
	cat $PROMETHEUS_YAML
}


#
# Deploy the specified number of instances of petclinic on a given cluster type
#
# Input - Number of instances of petclinic
#

function deploy_petclinic() {
	echo
	num_instances=$1
	if [ $num_instances == 1 ]; then
	        echo "Deploying $num_instances instance of spring petclinic app..."
	else
	        echo "Deploying $num_instances instances of spring petclinic app..."
	fi

	if [ $cluster_type == "docker" ]; then
		echo
		
		for(( inst=0; inst<${num_instances}; inst++ ))
	        do
			echo "  - name: \"petclinic-app-$inst\"" >> "$KRUIZE_REPO/manifests/docker/kruize-docker.yaml"
		done
		cat "$KRUIZE_REPO/manifests/docker/kruize-docker.yaml"

		# Invoke the deploy script from petclinic benchmark
		$APP_REPO/spring-petclinic/scripts/petclinic-deploy-docker.sh -i $num_instances
	else 
		MANIFESTS_DIR="$APP_REPO/spring-petclinic/manifests"	
		if [ $cluster_type == "icp" ]; then
			app_ns="cert-manager"
		fi
		cmd="kubectl"
		login_cmd="cloudctl"
		url=$kurl

		if [[ $cluster_type == "icp" || $cluster_type == "openshift" ]]; then
			if [ $cluster_type == "openshift" ]; then
				cmd="oc"
				login_cmd="oc"
				url="https://api.$kurl:6443"
				app_ns="openshift-monitoring"
				
			fi
			$login_cmd login ${url} -u ${user} -p ${password} -n $kruize_ns 
		fi


		# Deploy petclinic instances
		if [ $cluster_type == "openshift" ]; then
			$APP_REPO/spring-petclinic/scripts/petclinic-deploy-openshift.sh -s $kurl -i $num_instances
		elif [[ $cluster_type == "minikube" || $cluster_type == "icp" ]]; then
			$APP_REPO/spring-petclinic/scripts/petclinic-deploy-minikube.sh -i $num_instances
		fi

	fi
	echo "done"

}

#
# Run jmeter load for petclinic application
#
# Input - Number of instances of petclinic
#

function run_petclinic_jmeter_load() {
	num_instances=$1

        echo
        echo "Starting petclinic jmeter workload..."

	if [[ $cluster_type == "openshift" || $cluster_type == "docker" || $cluster_type == "minikube" ]]; then

		# Invoke the jmeter load script
		MAX_LOOP=1
		$APP_REPO/spring-petclinic/scripts/petclinic-load.sh -c $cluster_type -i $num_instances -l $MAX_LOOP

	elif [[ $cluster_type == "icp" || $cluster_type == "minikube" ]]; then
		SERVER=$(echo $kurl | awk -F[/:] '{print $4}')
	        echo "SERVER = $SERVER"

		MAX_LOOP=2

		# Invoke the jmeter load script
		$APP_REPO/spring-petclinic/scripts/petclinic-load.sh -c $cluster_type -i $num_instances -l $MAX_LOOP -a $SERVER
	fi
}


#
# Form the curl command for kruize 
#

function form_curl_cmd() {
        KRUIZE_PORT=31313
       	AUTH_TOKEN=""
	
	# Form the curl command based on the cluster type	
	if [ $cluster_type == "icp" ]; then
		ICP_SERVER=$(echo $kurl | awk -F[/:] '{print $4}')
        	KRUIZE_URL="http://$ICP_SERVER"
	elif [ $cluster_type == "openshift" ]; then
		KRUIZE_URL="kruize-openshift-monitoring.apps.$kurl"
	elif [ $cluster_type == "minikube" ];then
		KRUIZE_URL=$kurl
	elif [ $cluster_type == "docker" ]; then
		KRUIZE_URL="http://localhost"
	fi


	if [ $cluster_type == "openshift" ]; then
	        curl_cmd="curl -s -H 'Authorization:Bearer $AUTH_TOKEN' $KRUIZE_URL"
	else
        	curl_cmd="curl -s -H 'Authorization:Bearer $AUTH_TOKEN' $KRUIZE_URL:$KRUIZE_PORT"
	fi

}


#
# Get Kruize recommendations for the specified application
#
# Input - Application name
# Output - JSON recommendations array
#

function get_recommendation_for_application() {
	# Fetch recommendations for the specified application
        ${curl_cmd}/recommendations?application_name=$1 | jq -r '.[]'
}

#
# Get the list of Applications monitored by kruize
#
# Output - JSON array with list of applications
#

function get_list_of_applications {
        ${curl_cmd}/listApplications | jq -c '.[]'
}


#
# Validates the actual application status against the expected status for a specified application
#
# Input - app, expected_status
# Output - Returns success or failure
#

function validate_app_status() {
	app=$1
	expected_status=$2
	for row in $(get_list_of_applications); do
                application_name=$(echo "${row}" | jq -r '.application_name')
                app_status=$(echo "${row}" | jq -r '.status')
                if [ "$application_name" == "$app" ]; then
			echo "Application $app found in listApplications, checking its status..."
                        if [ "$app_status" == "$expected_status" ]; then
				echo "Application status - $app_status matches with the expected status - $expected_status"
                                return 0
                        else
				echo "Application status - $app_status does not match with the expected status - $expected_status"
                                return 1
                        fi
                fi
        done
	echo "Failed - Application $app not found in listApplications!"
	return 1
}


#
# Fetches the memory request for a specified application from kruize using curl command
#
# Input - application_name, param - parameter for eg., memory request (.resources.requests.memory)
# Output - cpu or memory value
#

function get_resource_value() {
	# Fetches the value for the specified parameter (for eg., mem/cpu req/limit) for the specified application
	param=$2
        #${curl_cmd}/recommendations?application_name=$1 | jq -r '.[]' |  jq -r '.resources.requests.memory' | awk -F[M] {'print($1)'}
        ${curl_cmd}/recommendations?application_name=$1 | jq -r '.[]' |  jq -r $param | awk -F[M] {'print($1)'}
}

#
# Fetches the java runtime recommendations for a specified application from kruize using curl command
#
# Input - application name
# Output - java runtime recommendations
#

function get_java_runtime_recommendations() {
	# Fetch the runtime recommendations for the specified application
        ${curl_cmd}/recommendations?application_name=$1 | jq -r '.[]' |  jq -r '.resources.env[0].value' 
}

#
# Check if the specified value of a parameter is 0 
#
# Input - param - parameter name (for eg., cpu/mem req/limit), value, app - application
# Output - Returns 1 if value is zero otherwise 0
# 

function isZero() {
	param=$1
	value=$2
	app=$3

	result=`echo "$value > 0.0" | bc`
	if [ "$result" == "0" ]; then
		echo "$param for $app is 0"		
		return 1
	else 
		return 0
	fi
}

#
# Validate kruize recommendations - memory and cpu reqs/limits and heap recommendations are non-zero for a specified application
#
# Input - Application name
# Output - Returns success or failure
#

function validate_resource_params() {
	application_name=$1
	resource_status=0

	echo "Validating resources parameters..."

	# Fetch the resource requests and limits
	mem_req=$(get_resource_value ${application_name} '.resources.requests.memory')
	echo "Memory Request = $mem_req"

        cpu_req=$(get_resource_value ${application_name} '.resources.requests.cpu')
	echo "CPU Request = $cpu_req"

	mem_lim=$(get_resource_value ${application_name} '.resources.limits.memory')
	echo "Memory Limit = $mem_lim"

        cpu_lim=$(get_resource_value ${application_name} '.resources.limits.cpu')
	echo "CPU Limit = $cpu_lim"

        # Validate if kruize recommendations are non-zero
	isZero "Memory Request" $mem_req ${application_name}
	if [ $? != 0 ]; then
		resource_status=1
        fi

	isZero "CPU Request" $cpu_req ${application_name}
	if [ $? != 0 ]; then
		resource_status=1
        fi


	isZero "Memory Limit" $mem_lim ${application_name}
	if [ $? != 0 ]; then
		resource_status=1
        fi

	isZero "CPU Limit" $cpu_lim ${application_name}
	if [ $? != 0 ]; then
		resource_status=1
        fi


	if [[ $application_name =~ "petclinic" ]]; then
	        runtime_recos=$(get_java_runtime_recommendations $application_name)
		echo "runtime_recommendations = $runtime_recos"
		runtime=($(echo $runtime_recos | tr ' ' '\n'))

		initial_ram=$(echo "${runtime[0]}" | awk -F "=" {'print($2)'})
		max_ram=$(echo "${runtime[1]}" | awk -F "=" {'print($2)'})
		gcpolicy=$(echo "${runtime[2]}" | awk -F ":" {'print($2)'})
		echo "initial_ram = $initial_ram max_ram = $max_ram gcpolicy=$gcpolicy"

		isZero "InitialRAMPercentage" $initial_ram ${application_name}
		if [ $? != 0 ]; then
			resource_status=1
		fi

		isZero "MaxRAMPercentage" $max_ram ${application_name}
		if [ $? != 0 ]; then
			resource_status=1
		fi

		result=`echo "$initial_ram == $max_ram" | bc`
		if [ "$result" == "0" ]; then
			echo "Failed - InitialRAMPercentage is not equal to MaxRAMPercentage"
			resource_status=1
		fi

		expected_gcpolicy="gencon"
		if [ $gcpolicy != $expected_gcpolicy ]; then
			echo "Failed - GC Policy is not as expected! Expected - $expected_gcpolicy Actual - $gcpolicy"
			resource_status=1
		fi
	fi

	return $resource_status
}

#
# Validate kruize recommendations for applications for which kruize has generated recommendations
#
# Output - Returns success or failure
#

function validate_all_recommendations() {
	invalid=0
	no_recos=0
        for row in $(get_list_of_applications); do
                application_name=$(echo "${row}" | jq -r '.application_name')
                recommendations_generated=$(echo "${row}" | jq -r '.recommendations_generated')
		echo "Get recommendations for application $application_name..."
		echo
		reco=$(get_recommendation_for_application ${application_name})
		echo $reco
		echo
		if [[ "$recommendations_generated" == "yes" ]]; then
			no_recos=1
			validate_resource_params ${application_name}
			if [ $? != 0 ]; then
				echo "Failed -  Kruize generated reommendations aren't valid for ${application_name}"	
				invalid=1
			fi
			
		fi
	done
	
	if [[ $invalid == 1 || $no_recos == 0 ]]; then
		if [ $no_recos == 0 ]; then
			echo "Failed - no recommendations_generated, check logs for details"
		fi
		return 1

	else 
		return 0
	fi
}


#
# Validate kruize recommendations for a specified application. Also validate the actual application status against expected status
#
# Input - Application, Expected Application status
# Output - Returns success or failure
#

function validate_recommendations() {
        echo
	app=$1
	app_status=$2

	echo "Application=$app curl_cmd=${curl_cmd} app_status=$app_status"
	echo
	echo "**************************************************"
	echo "List of Appplications..."
	echo "**************************************************"
	echo
	get_list_of_applications
	echo
	echo "**************************************************"
	echo

	echo "Get recommendations for application $app..."
	reco=$(get_recommendation_for_application ${app})
	if [ $? != 0 ]; then
		echo "Failed - Error getting recommendations for $app!"
		echo $reco
		return 1
	fi
	echo $reco

	echo	
	echo "Validate resource recommendations..."
	validate_resource_params ${app}
	if [ $? != 0 ]; then
		echo "Failed - Some of the kruize recommendations generated was 0, check logs for details!" 	
		return 1
	else 
		return 0
	fi
}

#
# Get kruize log and store it kruize.log in results dir
#

function get_kruize_log(){
	log="$RESULTS_DIR/kruize.log"
	if [[ $cluster_type == "icp" || $cluster_type == "minikube" ]]; then
		pod=`kubectl get pods -n $kruize_ns | grep "kruize" | awk '{print $1}'`
		echo "Getting logs for pod $pod..."
		kubectl logs $pod -n $kruize_ns > $log
	elif [ $cluster_type == "openshift" ]; then 
		pod=`oc get pods -n $kruize_ns | grep "kruize" | awk '{print $1}'`
		echo "Getting logs for pod $pod..."
		oc logs $pod -n $kruize_ns > $log
	elif [ $cluster_type == "docker" ]; then
		echo "Getting logs for kruize..."
		docker logs kruize > $log
	fi	
}


#
# Invoke the deploy script of kruize to terminate kruize. And delete the route in openshift case
#

function kruize_cleanup() {

	pushd $KRUIZE_REPO > /dev/null
        ./deploy.sh -ac $cluster_type -t
	if [ $cluster_type == "openshift" ]; then
		oc delete route kruize -n $kruize_ns
	fi
        popd > /dev/null
}

#
# Backup prometheus.yaml for docker case
#

function backup_prometheus_yaml() {
	# Backup prometheus.yaml
	echo "Taking a backup of $KRUIZE_REPO/manifests/docker/prometheus.yaml..."
	cp "$KRUIZE_REPO/manifests/docker/prometheus.yaml" "$KRUIZE_REPO/manifests/docker/prometheus-old.yaml"
}

#
# Restore the original prometheus.yaml for docker case
#

function restore_prometheus_yaml() {
	# Restore the original prometheus.yaml
	if [ -e "$KRUIZE_REPO/manifests/docker/prometheus-old.yaml" ]; then
		echo "Restoring $KRUIZE_REPO/manifests/docker/prometheus.yaml..."
		mv "$KRUIZE_REPO/manifests/docker/prometheus-old.yaml" "$KRUIZE_REPO/manifests/docker/prometheus.yaml"
		echo
	fi
}

#
# Backup kruize-docker.yaml
#

function backup_kruize_docker_yaml() {
	# Backup kruize-docker.yaml
	echo "Taking a backup of $KRUIZE_REPO/manifests/docker/kruize-docker.yaml..."
	cp "$KRUIZE_REPO/manifests/docker/kruize-docker.yaml" "$KRUIZE_REPO/manifests/docker/kruize-docker-old.yaml"
}


#
# Restore the original kruize-docker.yaml
#

function restore_kruize_docker_yaml() {
	# Restore the original kruize-docker.yaml
	if [ -e "$KRUIZE_REPO/manifests/docker/kruize-docker-old.yaml" ]; then
		echo "Restoring $KRUIZE_REPO/manifests/docker/kruize-docker.yaml..."
		mv "$KRUIZE_REPO/manifests/docker/kruize-docker-old.yaml" "$KRUIZE_REPO/manifests/docker/kruize-docker.yaml"
		echo
	fi
}

#
# Invoke the acmeair cleanup benchmark script for cleaning up acemair application  
#

function acmeair_cleanup() {
        echo
        echo "Removing acmeair app..."

	if [ $cluster_type == "docker" ]; then
		echo
        	pushd "$APP_REPO/acmeair/scripts" > /dev/null
	        ./acmeair-cleanup.sh
		popd > /dev/null

	else 
		app_ns="default"
                if [ $cluster_type == "icp" ]; then
                        app_ns="cert-manager"
                fi
                cmd="kubectl"
                login_cmd="cloudctl"
                url=$kurl


		if [[ $cluster_type == "icp" || $cluster_type == "openshift" ]]; then	
			if [ $cluster_type == "openshift" ]; then
				cmd="oc"
				login_cmd="oc"
				url="https://api.$kurl:6443"
			fi
			echo "$login_cmd $cmd $url $user $password $kruize_ns $app_ns $APP_REPO"
			$login_cmd login ${url} -u ${user} -p ${password} -n $kruize_ns 
		fi

	        $cmd delete -f $APP_REPO/acmeair/manifests/mongo-db.yaml -n $app_ns
	        $cmd delete -f $APP_REPO/acmeair/manifests/acmeair.yaml -n $app_ns
		if [[ $cluster_type == "openshift" ]]; then
			$cmd delete route acmeair-service -n $app_ns
		fi

	fi
	echo "done"
}


#
# Invoke the petclinic cleanup benchmark script by passing the cluster type
#

function petclinic_cleanup() {
	echo
        echo "Removing spring petclinic app..."
	if [[ $cluster_type == "minikube" || $cluster_type == "openshift" || $cluster_type == "icp" ]]; then	
		app_ns="default"
		if [ $cluster_type == "icp" ]; then
			app_ns="cert-manager"
		fi
		cmd="kubectl"
		login_cmd="cloudctl"
		url=$kurl

		if [[ $cluster_type == "icp" || $cluster_type == "openshift" ]]; then	
			if [ $cluster_type == "openshift" ]; then
				cmd="oc"
				login_cmd="oc"
				url="https://api.$kurl:6443"
				app_ns="openshift-monitoring"
				
			fi
			echo "$login_cmd $cmd $url $user $password $kruize_ns $app_ns $APP_REPO"
			$login_cmd login ${url} -u ${user} -p ${password} -n $kruize_ns 
		fi

		$APP_REPO/spring-petclinic/scripts/petclinic-cleanup.sh $cluster_type

		        
	fi
	echo "done"
}

###############################################################
