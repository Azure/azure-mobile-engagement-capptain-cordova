export PLUGIN_VERSION="1.0.3"
sed -i.bak "s/pluginVersion :.*/pluginVersion : \"$PLUGIN_VERSION\",/"  "www/Capptain.js"
sed -i.bak "s/CAPPTAIN_PLUGIN_VERSION .*/CAPPTAIN_PLUGIN_VERSION \"$PLUGIN_VERSION\"/"  "src/ios/Capptain.m"
sed -i.bak "s/pluginVersion = .*/pluginVersion = \"$PLUGIN_VERSION\";/"  "src/android/Capptain.java"
sed -i.bak "s/\"version\": .*/\"version\": \"$PLUGIN_VERSION\",/"  "package.json"
sed -i.bak "s/id=\"capptain-cordova\" version=.*/id=\"capptain-cordova\" version=\"$PLUGIN_VERSION\">/"  "plugin.xml"
sed -i.bak "s/id=\"capptain-cordova-tests\" version=.*/id=\"capptain-cordova-tests\" version=\"$PLUGIN_VERSION\">/"  "tests/plugin.xml"



