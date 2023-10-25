#!/usr/bin/sh

set -e

./make-keys.sh

rm -rf softhsm/*
source ./setup-softhsm.sh

mda.sh aggregate-and-sign.xml main
mda.sh filter-aggregate.xml main
mda.sh aggregate-and-republish.xml main
mda.sh sign-using-token.xml main
mda.sh per-entity.xml main
mda.sh discofeed.xml main
