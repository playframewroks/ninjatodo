ninjatodo
=========

Ninja To Do is a minimalist to do list in Java, originally ported from mytinytodo 1.4.2 in php (http://mytinytodo.net) and enhanced substantially thereafter. Ninja To Do uses the Play Framework 1.2.4 (http://playframework.org).

## Introduction

Agile development teams need a very light-weight to do list that also offers collaboration and a mobile-friendly UI. It would be even better if it is open source. I've searched for a long time and found mytinytodo fit 70% of the bill. But it is in php. I like Java. So I decided to port it to Java, and further streamline its UI as well as enhance its collaboration capabilities. If there is any features you'd like to see based on your agile practice, please let me know.

## Requirement

* JDK 1.6 or later
* Play Framework 1.2.4
* MySQL 5.1 or later

## Main Features

* Each user can create multiple workspaces, with each workspace shared among a group of users
* User permissions within each workspace.
* Multiple to do list in each workspace.
* Use @username tag to assign tasks to users
* Tons of filters to quickly find what you look for
* Very light weight UI
* Notify you when the to do list you watched has been updated by someone else
* Many more ...
* Mobile web friendly, and a native mobile client is coming soon ...

## Installation

* Install Play Framework according to its guide
* Install and run MySQL
* Configure conf/application.conf to have the correct MySQL url and user/password 
* (Option 1) Under the root folder of the project, run "play run" in command line. By default the server listens on port 9000. To change the port consult Play documentation
* (Option 2) Under the root folder of the project, run "play war" to generate a war file. The war file can be deployed in any Java servlet container.

