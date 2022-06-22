

﻿
# Kruize - Installation and Build

- [Installation](#installation)
  - [Docker](#docker)
  - [Kubernetes](#kubernetes)
    - [Minikube](#minikube)
    - [OpenShift](#openshift)
    - [IBM Cloud Private (ICP)](#ibm-cloud-private-(icp))
  - [Add Application Label](#add-application-label)
  - [Install the Grafana dashboard](#install-the-grafana-dashboard)
  - [Configure Logging Level](#configure-logging-level)
- [Build](#build)

# Installation

## Docker

Developing a microservice on your laptop and want to quickly size the application container using a test load ? Run the Kruize container locally and point it to your application container. Kruize monitors the app container using Prometheus and provides recommendations as a Grafana dashboard (Prometheus and Grafana containers are automatically downloaded when you run kruize).

```
$ ./deploy.sh -c docker

###   Installing kruize for docker...


Info: Checking pre requisites for Docker...
...

Waiting for kruize container to come up
########################     Starting App Monitor loop    #########################
Kruize recommendations available on the grafana dashboard at: http://localhost:3000
Info: Press CTRL-C to exit
 cadvisor: found. Adding to list of containers to be monitored.
 grafana: found. Adding to list of containers to be monitored.
 kruize: found. Adding to list of containers to be monitored.
 prometheus: found. Adding to list of containers to be monitored.


```

Now edit `manifests/docker/kruize-docker.yaml` to add the names of the containers that you need kruize to monitor.

```
$ cat manifests/docker/kruize-docker.yaml 
---
# Add names of the containers that you want kruize to monitor, one per line in double quotes
containers:
  - name: "cadvisor"
  - name: "grafana"
  - name: "kruize"
  - name: "prometheus"
  - name: "acmeair-mono-app1"
  - name: "acmeair-db1"
```

In the above example, kruize is monitoring the application containers `acmeair-mono-app1` and `acmeair-db1` as well as its own set of containers. You should now see the "App Monitor loop" listing the new containers to be monitored

```
 cadvisor: found. Adding to list of containers to be monitored.
 grafana: found. Adding to list of containers to be monitored.
 kruize: found. Adding to list of containers to be monitored.
 prometheus: found. Adding to list of containers to be monitored.
 acmeair-mono-app1: found. Adding to list of containers to be monitored.
 acmeair-db1: found. Adding to list of containers to be monitored.
```

You can now access the grafana dashboard at [http://localhost:3000](http://localhost:3000). Login as `admin/admin` and click on the pre-installed `Kruize Dashboard`. Select the application name from the `Deployment` drop down and you are all set !


## Kubernetes

Kruize can be deployed to a supported Kubernetes cluster. We currently support Minikube, IBM Cloud Private (ICP) and OpenShift.

### Minikube


```
$ ./deploy.sh -c minikube

###   Installing kruize for minikube


Info: Checking pre requisites for minikube...
Info: kruize needs cadvisor/prometheus/grafana to be installed in minikube
Download and install these software to minikube(y/n)? y                     <----- Say yes to install cadvisor/prometheus/grafana
Info: Downloading cadvisor git
...

Info: Downloading prometheus git

Info: Installing prometheus
...

Info: Waiting for all Prometheus Pods to get spawned......done
Info: Waiting for prometheus-k8s-1 to come up...
prometheus-k8s-1                      2/3     Running   0          5s
Info: prometheus-k8s-1 deploy succeeded: Running
prometheus-k8s-1                      2/3     Running   0          6s


Info: One time setup - Create a service account to deploy kruize
serviceaccount/kruize-sa created
clusterrole.rbac.authorization.k8s.io/kruize-cr created
clusterrolebinding.rbac.authorization.k8s.io/kruize-crb created
servicemonitor.monitoring.coreos.com/kruize created
prometheus.monitoring.coreos.com/prometheus created

Info: Deploying kruize yaml to minikube cluster
deployment.apps/kruize created
service/kruize created
Info: Waiting for kruize to come up...
kruize-695c998775-vv4dn               0/1     ContainerCreating   0          4s
kruize-695c998775-vv4dn               1/1     Running   0          9s
Info: kruize deploy succeeded: Running
kruize-695c998775-vv4dn               1/1     Running   0          9s

Info: Access grafana dashboard to see kruize recommendations at http://localhost:3000 <--- Click on this link to access grafana dashboards
Info: Run the following command first to access grafana port
      $ kubectl port-forward -n monitoring grafana-58dc7468d7-rn7nx 3000:3000		<---- But run this command first

```

After the installation completes successfully, run the `port-forward` command as shown. This is needed to access the grafana service. Now click on the [http://localhost:3000](http://localhost:3000) to access grafana. Login as `admin/admin`, navigate to `Create` from the left bar, then click on `Import`. Click on `Upload .json file` and point it to the [dashboard](/grafana/kruize_kubernetes_dashboard.json) file that is part of this repo. Once installed, select `Kruize Dashboard`. Select the application name from the `Deployment` drop down and you are all set !

Note: Kruize only monitors application with a specific label. See [Add Application Label](#add-application-label) below for more info.


### OpenShift


```
$ ./deploy.sh -c openshift

###   Installing kruize for OpenShift

WARNING: This will create a Kruize ServiceMonitor object in the openshift-monitoring namespace
WARNING: This is currently not recommended for production

Create ServiceMonitor object and continue installation?(y/n)? y

Info: Checking pre requisites for OpenShift...done
Info: Logging in to OpenShift cluster...
Authentication required for https://aaa.bbb.com:6443 (openshift)
Username: kubeadmin
Password: 
Login successful.

You have access to 52 projects, the list has been suppressed. You can list all projects with 'oc projects'

Using project "kube-system".

Info: Setting Prometheus URL as https://prometheus-k8s-openshift-monitoring.apps.kaftans.os.fyre.ibm.com
Info: Deploying kruize yaml to OpenShift cluster
Now using project "openshift-monitoring" on server "https://api.kaftans.os.fyre.ibm.com:6443".
deployment.extensions/kruize configured
service/kruize unchanged
Info: Waiting for kruize to come up...
kruize-5cd5967d97-tz2cb                        0/1     ContainerCreating   0          6s
kruize-5cd5967d97-tz2cb                        0/1     ContainerCreating   0          13s
kruize-5cd5967d97-tz2cb                        1/1     Running   0          20s
Info: kruize deploy succeeded: Running
kruize-5cd5967d97-tz2cb                        1/1     Running   0          24s
```

Now you need to install the Kruize Dashboard, see the [Install the Grafana dashboard](#install-the-grafana-dashboard) section for more details. 

Note: OpenShift versions <=4.3 do not support adding additional dashboards to Grafana and in that case visualization through the Kruize dashboard is not currently supported. Versions 4.4 onwards, there is a separate Prometheus/Grafana instance that can be deployed and used to monitor applications (currently tech preview only).

Note: Kruize only monitors application with a specific label. See [Add Application Label](#add-application-label) below for more info.


### IBM Cloud Private (ICP)


```
$ ./deploy.sh -c icp

Info: Checking pre requisites for ICP...done
Info: Logging in to ICP cluster...
API endpoint: https://aaa.bbb.com:8443

Username> admin

Password> 
Authenticating...
OK

Targeted account mycluster Account (id-mycluster-account)

Select a namespace:
1. cert-manager
2. default
3. istio-system
4. kube-public
5. kube-system    <---- Choose the kube-system namespace.
6. platform
7. services
Enter a number> 5
Targeted namespace kube-system

Configuring kubectl ...
Property "clusters.mycluster" unset.
Property "users.mycluster-user" unset.
Property "contexts.mycluster-context" unset.
Cluster "mycluster" set.
User "mycluster-user" set.
Context "mycluster-context" created.
Switched to context "mycluster-context".
OK

Configuring helm: /home/dino/.helm
OK

Info: Setting Prometheus URL as https://aaa.bbb.com:8443/prometheus
Info: Deploying kruize yaml to ICP cluster
deployment.extensions/kruize configured
service/kruize unchanged
kruize-866d48ddd-5qw2v                                         1/1       Running             0          10s
```

From the ICP main menu navigate to Grafana (Eg Menu -> Platform -> Monitoring). Now you need to install the Kruize Dashboard, see [Install the Grafana dashboard](#install-the-grafana-dashboard) for more details.

Note: Kruize only monitors application with a specific label. See [Add Application Label](#add-application-label) below for more info.


### Add Application Label

kruize is now ready to monitor applications in your cluster ! Note however that kruize only monitors applications with the label `app.kubernetes.io/name: "myapp"` currently.
```
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: myapp
        name: myapp
        app.kubernetes.io/name: "myapp"    <--- Add this label to your app yaml
```

### Install the Grafana dashboard

Login to your Grafana dashboard and navigate to `Create` from the left bar, then click on `Import`. Click on `Upload .json file` and for docker point it to the [docker dashboard](/manifests/docker/grafana/dashboards/kruize_docker_dashboard.json) file and for kubernetes point it to the [kubernetes dashboard](/grafana/kruize_kubernetes_dashboard.json) file that is part of this repo.

![Import dashboard into Grafana](/docs/grafana-import.png)

Once imported, the grafana dashboard should look something like this.

![Kruize Grafana Dashboard](/docs/grafana-dash.png)

Once installed, select `Kruize Dashboard`. Select the application name from the `Deployment` drop down and you are all set !

### Configure Logging Level

Kruize uses slf4j and the log4j-slf4j binding for its logging. The log levels used are:

| Logging Level | Description                                                                                                                         |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `ERROR`       | Error events that stop application from running correctly.                                                                          |
| `WARN`        | Designates potentially harmful situations. Includes `ERROR` logs.                                                                   |
| `INFO`        | Informational messages that highlight the progress of the application. Includes `ERROR` and `WARN` logs. The default logging level. |
| `DEBUG`       | Designates fine-grained informational events that are most useful to debug an application. Includes logs from `INFO` level.         |
| `ALL`         | Turn on all logging.                                                                                                                |

By default, the log level is set to `INFO`. To change the logging level, set the ENV `LOGGING_LEVEL` in `manifests/kruize.yaml_template` to any of the above levels. 


```
        - name: MONITORING_AGENT
          value: "prometheus"
        - name: MONITORING_SERVICE
          value: "{{ MONITORING_SERVICE }}"
        - name: LOGGING_LEVEL
          value: "INFO"          <--- Change this to any of the above levels
```

While submitting issues, we recommend users to attach the logs with log level set to `DEBUG`.

## Building Kruize

```
$ ./build.sh
```
Tag it appropriately and push it to a docker registry that is accessible to the kubernetes cluster. Don't forget to update the manifest yaml's to point to your newly built image !

