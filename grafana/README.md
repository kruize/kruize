
## Grafana Dashboard

There are 2 json files provided for the Kruize Grafana dashboard depending upon the cluster type: 

1. `kruize_docker_dashboard.json` for Docker cluster type 
2. `kruize_kubernetes_dashboard.json` for Kubernetes cluster type 

The differences between the 2 json files are: 

1. `kruize_docker_dashboard.json` has the value of key `datasource` as `Prometheus` whereas 
`kruize_kubernetes_dashboard.json` has the value of key `datasource` as `prometheus`. This is because the name of 
Prometheus data source in Grafana is `Prometheus` by default for Docker use case and `prometheus` by default for 
Kubernetes use case. 
2. The query for the deployment variable for the Docker use case is `label_values(application_name)` whereas for the 
Kubernetes use case it is `label_values(kube_deployment_labels{namespace=~"$namespace"},deployment)`. This is because 
the metric name `kube_deployment_labels` is exposed by the `kube-state-metrics` application which is present as a 
deployment in Kubernetes use case but not in Docker use case. 
