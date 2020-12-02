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
# Kruize functional tests

# source the common functions scripts
. ${SCRIPTS_DIR}/common-functions.sh


#
# Multiple instances test - This test deploys multiple instances of petclinic and validates if kruize generates
# recommendations for all instanes
#
# Output - Returns the test status
#

function multiple_instances_test() {
	local failed=0
	# Deploy multiple applications and monitor using kruize		
	echo
	echo "*********************************************************************************************************"
	echo "						Multiple Instances Test 				       "
	echo "Validate kruize recommendations and listApplications end points for multiple instances of an applications"
	echo "*********************************************************************************************************"
	echo


	# Cleanup existing instances
	petclinic_cleanup

	# Backup kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		backup_kruize_docker_yaml
	fi
	
	# Deploy petclinic application
	num_instances=3
	deploy_petclinic $num_instances
	sleep 60

	run_petclinic_jmeter_load $num_instances
	sleep 60

	for (( inst=0; inst<${num_instances}; inst++))
	do

		# Check if recommendations are generated for the deployed application
		app_status="deployed"
		app="petclinic-sample-$inst"
		if [ $cluster_type == "docker" ]; then
			app="petclinic-app-$inst"
		fi

		echo
		echo "Validate if kruize has generated recommendations for $app..."	
		validate_recommendations $app $app_status
		result=$?
		if [ $result -ne 0 ]; then
			failed=1
		fi

		# Check the status of the removed application using listApplications end point
		expected_status="running"
		echo
		echo "Validate application status of $app in listApplications..."
		validate_app_status $app $expected_status
		result=$?
		if [ $result -ne 0 ]; then
			failed=1
		fi
	done

	# Cleanup existing instances
	petclinic_cleanup

	# Restore kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		restore_kruize_docker_yaml
	fi

	echo
	if [ $failed -ne 0 ]; then
		echo "Multiple Instances Test - Failed"
	else
		echo "Multiple Instances Test - Passed"
	fi

	echo
	echo "*********************************** Multiple Instances Test done ****************************************"
	echo

	return $failed
}

#
# This function tests the kruize recommendations and listApplications APIs by deploying both acmeair and spring-petclinic applications. The test does the following:
# Deploys acmeair application
# Deploys petclinic application
# Runs the jmeter workload for both these applications
# Validates kruize recommendations for both these applications 
#
# Output - Returns the test status
#

function multiple_apps_test() {
	local failed=0

	# Deploy multiple applications and monitor using kruize		
	echo
	echo "*********************************************************************************************************"
	echo " 					Multiple Applications Test					       "
	echo "Validate kruize recommendations and listApplications end points for multiple applications"
	echo "*********************************************************************************************************"
	echo

	# Cleanup acmeair
	acmeair_cleanup

	# Backup kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		backup_kruize_docker_yaml
	fi
	
	# Deploy acmeair application
	deploy_acmeair 
	sleep 60

	# Cleanup existing instances
	petclinic_cleanup

	# Deploy petclinic application
	num_instances=1
	deploy_petclinic $num_instances
	sleep 60

	# Run the jmeter workload
	run_acmeair_jmeter_load

	# Run the jmeter workload
	run_petclinic_jmeter_load $num_instances
	sleep 60

	# Check if recommendations are generated for the deployed application
	app="acmeair-sample"
	if [ $cluster_type == "docker" ]; then
		app="acmeair-mono-app1"
	fi
	
		
	app_status="deployed"
	echo
	echo "Validate if kruize has generated recommendations for $app..."	
	validate_recommendations $app $app_status
	result=$?
	if [ $result -ne 0 ]; then
		failed=1
	fi

	# Check the status of the removed application using listApplications end point
	expected_status="running"
	echo
	echo "Validate application status in listApplications..."
	validate_app_status $app $expected_status
	result=$?
	if [ $result -ne 0 ]; then
		failed=1
	fi

	
	# Check if recommendations are generated for the deployed application
	app="petclinic-sample-0"
	if [ $cluster_type == "docker" ]; then
		app="petclinic-app-0"
	fi

	app_status="deployed"
	echo
	echo "Validate if kruize has generated recommendations for $app"	
	validate_recommendations $app $app_status
	result=$?
	if [ $result -ne 0 ]; then
		echo "Failed - kruize did not generate recommendations for $app"
		failed=1
	fi

	# Check the status of the removed application using listApplications end point
	expected_status="running"
	echo
	echo "Validate if application status in listApplications..."
	validate_app_status $app $expected_status
	result=$?
	if [ $result -ne 0 ]; then
		failed=1
	fi

	# Cleanup acmeair
	acmeair_cleanup

	# Cleanup existing instances
	petclinic_cleanup


	# Restore kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		restore_kruize_docker_yaml
	fi

	echo
	if [ $failed -ne 0 ]; then
		echo "Multiple Apps Test - Failed"
	else
		echo "Multiple Apps Test - Passed"
	fi

	echo
	echo "*********************************** Multiple Apps test done *********************************************"
	echo

	return $failed
}


#
# This function deploys petclinic and removes it and then validates the status of petclinic application using listApplications Kruize API
#
# Output - Returns the test status
#

function remove_app_test() {
	local failed=0
	echo
	echo "*********************************************************************************************************"
	echo "						App Removal Test                                               "
	echo "Validate kruize recommendations and listApplications end points for an application that is removed       "
	echo "*********************************************************************************************************"
	echo

	# Backup kruize-docker.yaml 
	if [ $cluster_type == "docker" ]; then
		backup_kruize_docker_yaml
	fi

	# Deploy petclinic application
	num_instances=1
	deploy_petclinic $num_instances
	sleep 60

	# Remove petclinic application
	petclinic_cleanup 

	sleep 200

	# Check recommendations are not generated for the removed application
	app="petclinic-sample-0"
	if [ $cluster_type == "docker" ]; then
		app="petclinic-app-0"
	fi

	app_status="removed"
	echo
	echo "Validate kruize recommendations for an application ($app) that was removed..."	
	validate_recommendations $app $app_status
	result=$?
	if [ $result -ne 0 ]; then
		failed=1
	fi

	# Check the status of the removed application using listApplications end point
	expected_status="terminated"
	echo
	echo "Validate application status in listApplications..."
	validate_app_status $app $expected_status 
	result=$?
	if [ $result -ne 0 ]; then
		failed=1
	fi

	# Restore kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		restore_kruize_docker_yaml
	fi

	echo
	if [ $failed -ne 0 ]; then
		echo "App Removal Test - Failed"
	else
		echo "App Removal Test - Passed"
	fi

	echo
	echo "************************************* App Removal test done *********************************************"
	echo

	return $failed
}

#
# This function tests the kruize recommendations and listApplications APIs by deploying a new application. The test does the following:
# Deploys petclinic application
# Runs the jmeter workload
# Validates kruize recommendations for the new application
# Also validates listApplications kruize API and kruize recommendations generated for other applications
# 
# Output - Returns the test status
#

function monitor_newapp_test() {
	local failed=0

	echo
	echo "*********************************************************************************************************"
	echo "						Monitor New App Test                                           "
	echo "Validate kruize recommendations and listApplications end points for a new application                    "
	echo "*********************************************************************************************************"
	echo


	# Remove petclinic application
	petclinic_cleanup  

	# Backup kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		backup_kruize_docker_yaml
	fi

	# Deploy petclinic application
	num_instances=1
	deploy_petclinic $num_instances
	sleep 60

	# Run the jmeter workload
	run_petclinic_jmeter_load $num_instances
	sleep 30

	# Check if recommendations are generated for the deployed application
	app="petclinic-sample-0"
	if [ $cluster_type == "docker" ]; then
		app="petclinic-app-0"
	fi

	app_status="deployed"
	echo
	echo "Validate if kruize has generated recommendations for $app..."
	validate_recommendations $app $app_status
	result=$?
	if [ $result == 1 ]; then
		failed=1
		echo "failed=$failed"
	fi

	
	# Check the status of the removed application using listApplications end point
	expected_status="running"
	echo
	echo "Validate application status in listApplications..."
	validate_app_status $app $expected_status
	result=$?
	if [ $result == 1 ]; then
		failed=1
		echo "failed=$failed"
	fi

	# Validate all recommendations generated by kruize
	echo
	echo "Validate all kruize generated recommendations..."
	validate_all_recommendations 
	result=$?
	if [ $result == 1 ]; then
		failed=1
		echo "failed=$failed"
	fi

	# Remove petclinic application
	petclinic_cleanup 

	# Restore kruize-docker.yaml
	if [ $cluster_type == "docker" ]; then
		restore_kruize_docker_yaml
	fi

	if [ $failed != 0 ]; then
		echo
		echo "Monitor new App Test - Failed"	
		echo
	else
		echo
		echo "Monitor new App Test - Passed"
		echo
	fi

	echo
	echo "***********************************  Monitor New App test done ******************************************"
	echo

	return $failed
}

#
# Setup function checks for pre-reqs, backs up prometheus yaml and updates it for docker case and deploys kruize
#

function setup() {
	# Create results directory
	RESULTS_DIR="$resultsdir/kruize-$tctype-tests-$cluster_type-$(date +%Y%m%d%H%M)"
	mkdir -p $RESULTS_DIR 
	echo "$RESULTS_DIR"

	# Check if jq is installed
	check_prereq

	# Backup and update prometheus yaml to get Java Heap recommendations for petclinic
	if [ $cluster_type == "docker" ]; then
		backup_prometheus_yaml
		update_prometheus_yaml
	fi

	# Deploy kruize 
	deploy_kruize 
}

#
# This function run only the monitor new app test as a sanity test for validating kruize recommendations
#
function sanity_test() {
	# Call the setup routine
	setup

	set -o pipefail
	# Test to validate if kruize can monitor any new application deployed
	monitor_newapp_test | tee -a "$RESULTS_DIR/monitor_newapp.log"
	ret=$?
	set +o pipefail

	# Get kruize logs
	get_kruize_log
	
	# Remove kruize 
	kruize_cleanup

	if [ $ret != 0 ]; then
		echo
		echo "Kruize Sanity Test Failed!"
		echo
		exit 1
	else
		echo
		echo "Kruize Sanity Test Passed!"
		echo
	fi
}


#
# This function invokes all the functional tests after doing the required setup, reports final test result and does cleanup
#
function functional_test() {
	local failed=0
	# Call the setup routine
	setup

	set -o pipefail
	# Test to validate if kruize can monitor any new application deployed 
	monitor_newapp_test | tee -a "$RESULTS_DIR/monitor_newapp.log"
	if [ $? != 0 ]; then
		failed=1
	fi

	# Test to validate listApplications REST Endpoint for an application that has been removed
	remove_app_test | tee -a "$RESULTS_DIR/remove_app.log"
	if [ $? != 0 ]; then
		failed=1
	fi

	# Test to validate if kruize generates recommendations for multiple applications and the status of the application reported by listApplications
	multiple_apps_test | tee -a "$RESULTS_DIR/multiple_apps.log"
	if [ $? != 0 ]; then
		failed=1
	fi

	# Test to validate if kruize generates recommendations for multiple instances of an application and the status of the application reported by listApplications
	if [ $cluster_type != "docker" ]; then
	multiple_instances_test | tee -a "$RESULTS_DIR/multiple_instances.log"
		if [ $? != 0 ]; then
			failed=1
		fi
	fi

	set +o pipefail

	# Get kruize logs
	get_kruize_log

	# Remove kruize 
	kruize_cleanup

	if [ $failed != 0 ]; then
		echo
		echo "Kruize Functional Tests Failed!"
		echo
		exit 1
	else
		echo
		echo "Kruize Functional Tests Passed!"
		echo
	fi

}

#
# Clean up function to remove kruize and application deployments and restores prometheus yaml for docker case
#
function cleanup() {
	# Terminate kruize and other apps deployed
	kruize_cleanup
	acmeair_cleanup  
	petclinic_cleanup 

	# Restore prometheus yaml
	if [ $cluster_type == "docker" ]; then
		restore_prometheus_yaml
	fi
	
}

