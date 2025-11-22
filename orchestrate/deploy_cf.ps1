param()
set-StrictMode -Version Latest

if (-not $env:IBM_API_KEY) { Write-Error "Set environment variable IBM_API_KEY"; exit 1 }
if (-not $env:IBM_REGION) { $env:IBM_REGION = "us-south" }
if (-not $env:CF_ORG) { Write-Error "Set environment variable CF_ORG"; exit 1 }
if (-not $env:CF_SPACE) { Write-Error "Set environment variable CF_SPACE"; exit 1 }

$APP_NAME = $env:APP_NAME
if (-not $APP_NAME) { $APP_NAME = "crm-server" }

Write-Host "Building project..."
mvn -B -DskipTests package

Write-Host "Logging into IBM Cloud..."
ibmcloud login --apikey $env:IBM_API_KEY -r $env:IBM_REGION | Out-Null

Write-Host "Targeting org and space..."
ibmcloud target -o $env:CF_ORG -s $env:CF_SPACE | Out-Null

Write-Host "Pushing application '$APP_NAME' to Cloud Foundry using manifest.yml..."
ibmcloud cf push $APP_NAME -f manifest.yml

Write-Host "Deployment requested. Check app status with:"
Write-Host "  ibmcloud cf apps"
Write-Host "  ibmcloud cf logs $APP_NAME --recent"

