#!/system/bin/sh
#
# Script for clash setup
# - on-start.sh : On clash core started, setup proxy here
# - on-stop.sh : On clash core stoped, clean here
#
# Environments:
# - CLASH_HTTP_PORT: clash http proxy port
# - CLASH_SOCKS_PORT: clash socks proxy port
# - CLASH_REDIR_PORT: clash redir proxy port
# - CLASH_DNS_PORT: clash dns port
# - CLASH_UID: clash runing uid
# - CLASH_GID: clash running gid
# - PROXY_BLACKLIST_UID: blacklist uid
#

assert() {
    "$@"
    if [[ $? != 0 ]];then
        echo "Command $@ failure"
        exit 1
    fi
}

if [[ "${CLASH_REDIR_PORT}" = "" ]];then
    echo "Clash redir disabled"
    exit 1
fi

iptables -t nat -D OUTPUT -p tcp -j CLASH_LOCAL
iptables -t nat -D PREROUTING -p tcp -j CLASH_EXTERNAL

iptables -t nat -F CLASH_LOCAL
iptables -t nat -X CLASH_LOCAL

iptables -t nat -F CLASH_EXTERNAL
iptables -t nat -X CLASH_EXTERNAL

if [[ "${CLASH_DNS_PORT}" = "" ]];then
    exit 0
fi

iptables -t nat -D OUTPUT -p udp -j CLASH_DNS_LOCAL
iptables -t nat -D PREROUTING -p udp -j CLASH_DNS_EXTERNAL

iptables -t nat -F CLASH_DNS_LOCAL
iptables -t nat -X CLASH_DNS_LOCAL

iptables -t nat -F CLASH_DNS_EXTERNAL
iptables -t nat -X CLASH_DNS_EXTERNAL
