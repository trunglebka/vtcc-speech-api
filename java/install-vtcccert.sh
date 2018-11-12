#!/usr/bin/env bash
$JAVA_HOME/bin/keytool -import -file src/main/java/speech/cert/wwwvtccai.crt -alias wwwvtccai -keystore $JAVA_HOME/jre/lib/security/cacerts
#if keytool ask you for keystore password, please input: 'changeit' as password
#if keytool ask you to 'Trust this certificate?' please say 'yes'
#JAVA_HOME is your SDK location
