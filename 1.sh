#!/bin/bash

# JFrog repo prefix
REPO="leapfse4.jfrog.io/fse4team3"

# Map folder names to image names
declare -A services=(
	["fse4-t3-ui"]="some-id-frontend"
	["fse4-t3-FMTS"]="some-id-fmts"
	["fse4-t3-middle-tier"]="some-id-middle-tier"
	["fse4-t3-backend"]="some-id-backend"
)

# Loop through each folder
for folder in "${!services[@]}"; do
	IMAGE_NAME="${services[$folder]}"
	FULL_IMAGE="$REPO/$IMAGE_NAME:v4.2"

	echo "📁 Entering folder: $folder"
	cd "$folder" || {
		echo "❌ Failed to enter $folder"
		exit 1
	}

	echo "🔧 Building image: $FULL_IMAGE"
	docker build -t "$FULL_IMAGE" .

	echo "🚀 Pushing image: $FULL_IMAGE"
	docker push "$FULL_IMAGE"

	echo "✅ Done with: $FULL_IMAGE"
	cd - >/dev/null
done
