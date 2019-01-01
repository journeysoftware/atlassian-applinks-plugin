---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: home
---
## What it does
This program can establish an applink between Atlassian and 3rd party integrations on the user's behalf.  It is meant to serve as a companion app for applications needing access to the Atlassian API and want to save its users from having to manually configure applinks access through the Atlassian dashboard.

## How it works
The program will gain access to the applinks.spi following installation granted by the user, which allows the program to mimick the actions of Atlassian's dashboard-based applink wizard by registering an applink with the service using pre-configured settings of the integrating application.

|Configuration requirements|
|application.link.name|
|application.link.url|
|consumer.name|
|consumer.description|
|consumer.key|
|consumer.public.key|
|consumer.callback.url|
