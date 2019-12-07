
# Kruize - Installation and Build

- [Installation](#installation)
  - [Docker](#docker)
  - [Kubernetes](#kubernetes)
    - [Minikube](#minikube)
    - [OpenShift](#openshift)
    - [IBM Cloud Private (ICP)](#ibm-cloud-private-(icp))
  - [Add Application Label](#add-application-label)
  - [Install the Grafana dashboard](#install-the-grafana-dashboard)
- [Build](#build)

# Installation

## Docker

Developing a microservice on your laptop and want to quickly size the application container using a test load ? Run the Kruize container locally and point it to your application container. Kruize monitors the app container using Prometheus and provides recommendations as a Grafana dashboard (Prometheus and Grafana containers are automatically donwloaded when you run kruize).

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

```

Now you need to install the Kruize Dashboard, see the [Install the Grafana dashboard](#install-the-grafana-dashboard) section for more details.

Note: Kruize only monitors application with a specific label. See [Add Application Label](#add-application-label) below for more info.


### IBM Cloud Private (ICP)


```
$ ./deploy.sh -c icp

Info: Checking pre requisites for ICP...done
Info: Logging in to ICP cluster...
API endpoint: https://192.168.122.156:8443

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

Info: Setting Prometheus URL as https://192.168.122.156:8443/prometheus
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

Login to your Grafana dashboard and navigate to `Create` from the left bar, then click on `Import`. Click on `Upload .json file` and point it to the [dashboard](/grafana/kruize_kubernetes_dashboard.json) file that is part of this repo.

![Import dashboard into Grafana](/docs/grafana-import.png)

Once imported, the grafana dashboard should look something like this.

![Kruize Grafana Dashboard](/docs/grafana-dash.png)

Once installed, select `Kruize Dashboard`. Select the application name from the `Deployment` drop down and you are all set !


## Building Kruize

```
$ ./build.sh
```
Tag it appropriately and push it to a docker registry that is accessible to the kubernetes cluster. Don't forget to update the manifest yaml's to point to your newly built image !
