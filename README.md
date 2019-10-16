
# Kruize - Right Size and Optimize your Containers !


## Motivation

Are you an IT Admin and worry about

1. How do I right size my containers ?
2. How do I plan the K8s capacity that I need ?
3. How do I optimize my containers for best startup / resource usage / throughput ?
4. For any Runtime ?
5. In the age of Microservices ? (Think 100s of commits per day)

Look no further, Kruize is here to help !

Kruize needs to be deployed to your Kubernetes cluster in the same namespace as Prometheus. Kruize monitors application containers running in your Kubernetes cluster using metrics provided by Prometheus and offers recommendations on the right CPU and Memory `request` and `limit` values for your application container. The recommendations can be viewed in a Grafana dashboard on a per application basis. This helps IT admins to review and apply the recommendations knowing that their applications and even the cluster is optimally sized !

## Deploying Kruize

To deploy kruize to your kubernetes cluster, you need the run the `deploy.sh` script. Make sure you deploy `kruize` to the same namespace as `prometheus`.

```
$ ./deploy.sh

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
5. kube-system
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

kruize is now ready to monitor applications in your cluster ! Note however that kruize only monitors applications with the label `org.kubernetes.io/name: "myapp"` currently.
```
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: myapp
        name: myapp
        org.kubernetes.io/name: "myapp"    <--- Add this label to your app yaml
```

## Install the Grafana dashboard

Import the Grafana dashboard into your kubernetes cluster.

Copy the contents of the `/grafana/kruize_dashboard.json` and paste into the import screen shown below. You can also use `upload .json file` and point it to `/grafana/kruize_dashboard.json`.

![Import dashboard into Grafana](/docs/grafana-import.png)

Once imported, the grafana dashboard should look something like this.

![Kruize Grafana Dashboard](/docs/grafana-dash.png)

## Building Kruize

```
$ ./build.sh
```
Tag it appropriately and push it to a docker registry that is accessible to the kubernetes cluster. Don't forget to update the manifest yaml's to point to your newly built image !

## Contributing

Refer to [CONTRIBUTING.md](/CONTRIBUTING.md).

## License

Apache License 2.0, see [LICENSE](/LICENSE).
