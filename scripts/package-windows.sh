#!/usr/bin/env bash
# Construit une application Windows autonome (Java embarqué) dans target/jpackage/AgenceLocation
# et l'archive dans AgenceLocation-windows.zip.
# Prérequis : JDK 17+ (jpackage inclus). À lancer depuis Git Bash à la racine du projet.
set -euo pipefail

./mvnw -B clean package

rm -rf target/dist target/jpackage
mkdir -p target/dist
cp -r target/lib target/agence-location-javafx.jar target/dist/

jpackage --type app-image \
  --name AgenceLocation \
  --app-version 1.0.0 \
  --vendor "Agence Location" \
  --input target/dist \
  --main-jar agence-location-javafx.jar \
  --main-class agencelocation.agencelocationjavafx.Launcher \
  --java-options "-Dfile.encoding=UTF-8" \
  --dest target/jpackage

cp sql/agence_location.sql target/jpackage/AgenceLocation/

rm -f AgenceLocation-windows.zip
if command -v 7z >/dev/null 2>&1; then
  (cd target/jpackage && 7z a -tzip ../../AgenceLocation-windows.zip AgenceLocation >/dev/null)
else
  powershell.exe -NoProfile -Command "Compress-Archive -Path target/jpackage/AgenceLocation -DestinationPath AgenceLocation-windows.zip"
fi

echo "OK : target/jpackage/AgenceLocation/AgenceLocation.exe et AgenceLocation-windows.zip"
