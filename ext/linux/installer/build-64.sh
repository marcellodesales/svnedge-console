#!/usr/bin/env bash

VERSION=1.3.0
OS=linux-x86_64
PKG=/u1/hudson/pkg-tools/bin/pkg
BUILD_TEMP=/u1/hudson/linux64/trunk

case "$1" in

    'dev')
        REPOS=http://cu025.cubit.sp.collab.net/release/linux64
        UPDATES=$REPOS
        TAR=CollabNetSubversionEdge-${VERSION}-dev_${OS}.tar.gz
        ;;

    'stage')
        REPOS=http://pkg.collab.net/qa/linux64/
        UPDATES=$REPOS
        TAR=CollabNetSubversionEdge-${VERSION}-RC_${OS}.tar.gz
        ;;

    'release')
        REPOS=http://pkg.collab.net/qa/linux64/
        UPDATES=http://pkg.collab.net/release/linux64/
        TAR=CollabNetSubversionEdge-${VERSION}_${OS}.tar.gz
        ;;

    *)
        echo "Usage: $0 { dev | stage | release }"
        exit 1
        ;;
esac

rm -Rf $BUILD_TEMP
mkdir -p $BUILD_TEMP
cd $BUILD_TEMP

# Start with a fresh folder
rm -Rf csvn
mkdir csvn

# Initialize the local image
$PKG image-create -U -a collab.net=$REPOS csvn
$PKG -R `pwd`/csvn set-property title "CollabNet Subversion Edge"
$PKG -R `pwd`/csvn set-property description "Package repository for CollabNet Subversion Edge."
$PKG -R `pwd`/csvn set-property send-uuid True
$PKG -R `pwd`/csvn set-authority -O $REPOS collab.net

# Install our application and required packages
$PKG -R `pwd`/csvn install pkg
$PKG -R `pwd`/csvn install csvn

# Now prepare image for distribution
$PKG -R `pwd`/csvn set-authority -O $UPDATES collab.net
$PKG -R `pwd`/csvn rebuild-index
$PKG -R `pwd`/csvn purge-history

# Remove the UUID
cp csvn/.org.opensolaris,pkg/cfg_cache /tmp/cfg_cache
grep -v "^uuid =" /tmp/cfg_cache > csvn/.org.opensolaris,pkg/cfg_cache

# Remove the variant ARCH
cp csvn/.org.opensolaris,pkg/cfg_cache /tmp/cfg_cache
grep -v "variant.arch" /tmp/cfg_cache > csvn/.org.opensolaris,pkg/cfg_cache

# Cleanup content within the image
cd csvn
mv temp-data data
cd ".org.opensolaris,pkg"
rm -Rf download

# Build and upload the tarball
cd $BUILD_TEMP
rm $TAR
tar -czf $TAR csvn/
pbl.py upload -u mphippard -k 10f7fdb0-f258-1375-8113-b9bfb93d5b8c -l https://mgr.cubit.sp.collab.net/cubit_api/1 -p svnedge -t pub -r /Installers/linux --force $TAR