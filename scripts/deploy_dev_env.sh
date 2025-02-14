#!/bin/bash
_remote="aninfosys.asia"
_user="admin"

echo "-------- Local system name: $HOSTNAME"
echo "-------- Local date and time: $(date)"

echo
echo "--------  Running commands on remote host named $_remote ***"
echo
ssh -i ../ssh/ssh_anfovn_admin -p 228 $_user@$_remote <<'EOL'
	now="$(date)"
	name="$HOSTNAME"
	up="$(uptime)"
	echo "-------- Server name is $name"
	echo "-------- Server date and time is $now"
  cd dsd/digital-signage-backend
	git reset --hard HEAD
	git checkout develop
	git pull

  docker stack deploy dsd_dev --resolve-image always --compose-file docker/docker-compose-dev.yml --with-registry-auth

EOL
