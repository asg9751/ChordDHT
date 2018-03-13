#!/usr/bin/env bash
gnome-terminal -e "docker run --rm -it -e SYSTEM_TYPE=RegistrationServer chord"
gnome-terminal -e "docker run --rm -it -e SYSTEM_TYPE=ChordNode chord"
