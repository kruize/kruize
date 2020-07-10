# Runtime Recommendations
Kruize can help optimize your application by generating optimal runtime recommendations. 

- [Supported Runtimes](#supported-runtimes)
  - [Java](#java)
- [Exposing application runtime metrics](#exposing-application-runtime-metrics)
  - [Docker](#docker)
  - [Minikube](#minikube)
  - [OpenShift](#openshift)
  - [IBM Cloud Private](#ibm-cloud-private)

## Supported Runtimes

### Java
Kruize currently supports generating additional runtime options for applications bundling the Springboot actuator for OpenJ9.

##  Exposing application runtime metrics 
If your application exports runtimes metrics for a supported runtime, the metrics endpoint can be added as a scrape target for Prometheus, allowing Kruize to pick up the metrics, run its analysis and generate additional runtime recommendations. 

### Docker
Append the application scrape job to `manifests/docker/prometheus.yaml` before Kruize is deployed. 
```
- job_name: petclinic
  honor_timestamps: true
  scrape_interval: 2s
  scrape_timeout: 1s
  metrics_path: /manage/prometheus
  scheme: http
  static_configs:
  - targets:
    - petclinic:8081
```

### Minikube
Deploy a ServiceMonitor resource for your application after Prometheus and Kruize are deployed through the deploy script. 
```
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: petclinic
  labels:
    team: petclinic-frontend
spec:
  selector:
    matchLabels:
        app: petclinic-app
  endpoints:
    - port: petclinic-port
      path: '/manage/prometheus'
```

### OpenShift
Deploy a ServiceMonitor resource for your application. By default, OpenShift only monitors additional targets and deployments if they are in openshift-monitoring namespace.
```
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: petclinic
  labels:
    team: petclinic-frontend
spec:
  selector:
    matchLabels:
        app: petclinic-app
  endpoints:
    - port: petclinic-port
      path: '/manage/prometheus'
```

### IBM Cloud Private
Deploy the ServiceMonitor resource for your application as in the case above. You can also annotate your applications yaml file with the following to allow Prometheus to pick up your application metrics instead.
```
      annotations:
        prometheus.io/scrape: 'true'
        prometheus.io/port: '8080'
        prometheus.io/path: '/manage/prometheus'
```


