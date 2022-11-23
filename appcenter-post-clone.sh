#!/usr/bin/env bash

KEY_URL='https://www.dropbox.com/s/f9cm31hw2pdyt6a/debug.keystore?raw=1'

function downloadReleaseKey() {
  curl -L -o keys/debug.keystore "$KEY_URL"
}
downloadReleaseKey
