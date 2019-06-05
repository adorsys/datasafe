# Datasafe Private Space

This module is designed to provide access to the user private space. All document stored on this space are encrypted 
with secret keys held by the user. Additionally path of the document is also encrypted with path encryption key. 
Privatespace itself resides somewhere on storage according to user private profile.

# How it works
![How it works diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/how_it_works.puml&fmt=svg&vvv=1&sanitize=true)

## Writing private file with details
![Writing details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/private_write.puml&fmt=svg&vvv=1&sanitize=true)

## Reading private file with details
![Reading details](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/adorsys/datasafe/develop/docs/diagrams/high-level/private_read.puml&fmt=svg&vvv=1&sanitize=true)