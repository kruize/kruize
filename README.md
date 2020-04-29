
# Kruize - Right Size and Optimize your Containers ! [![Build Status](https://travis-ci.org/kruize/kruize.svg?branch=master)](https://travis-ci.org/kruize/kruize)


## Motivation

Docker and Kubernetes have become more than buzzwords and are now the defacto building block for any cloud. We are now seeing a major transformation in the industry as every product/solution/offering is being containerized as well as being made kubernetes ready (Hello YAML!). This is throwing up a new set of challenges that are unique to this changing environment.

One such issue is right sizing and optimizing containers. When applications are not sized appropriately in a k8s cluster, they either waste resources (oversized) or worse get terminated (undersized), either way could result in loss of revenue. Add to this the new microsevices model means making hundreds of code changes in a day. This means that a "let us size it once and for all" model will no longer work. Now imagine doing it across a number of language runtimes each with its own requirements to understand the real scale of the problem !

![DevOps Dilemma](/docs/devops-dilemma.png)

Kruize monitors application containers for resource usage. It has a analysis engine which predicts the right size for the containers that are being monitored and offers recommendations on the right CPU and Memory `request` and `limit` values. The recommendations can be viewed in a dashboard on a per application basis. This helps IT admins to review and apply the recommendations knowing that their applications and even the cluster is optimally sized !

## Supported Configurations

Kruize currently supports Linux and macOS.

Two primary methods of deployment are available:

1. Docker (Developer Mode)

Developing a microservice on your laptop and want to quickly size the application container using a test load ? Run the Kruize container locally and point it to your application container. Kruize monitors the app container using Prometheus and provides recommendations as a Grafana dashboard (Prometheus and Grafana containers are automatically donwloaded when you run kruize).

2. Kubernetes

Kruize can be deployed to a supported Kubernetes cluster. We currently support Minikube, IBM Cloud Private (ICP) and OpenShift. Kruize uses Prometheus as the metrics provider and provides recommendations through a Grafana dashboard. Prometheus and Grafana come pre-packaged with both ICP and OpenShift. 

Even though Kruize currently supports Prometheus as the metrics provider, it can easily be extended to scrape metrics from any other metrics provider such as new relic, splunk etc.


## Installation

See the [Install README](/docs/README.md) for more details on the installation.


## REST API

See the [API README](/docs/API.md) for more details on the Kruize REST API.


## Contributing

Refer to [CONTRIBUTING.md](/CONTRIBUTING.md).

## License

Apache License 2.0, see [LICENSE](/LICENSE).
