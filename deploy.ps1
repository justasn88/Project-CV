$yamlPath = ".\gcp-infra\prod\values-prod.yaml"

if (-Not (Test-Path $yamlPath)) {
    Write-Host "Error: File not found at $yamlPath. Please make sure you moved the file there." -ForegroundColor Red
    exit 1
}

$yaml = Get-Content $yamlPath | Out-String

if ($yaml -match 'tag:\s*"?([a-zA-Z0-9\.]+)"?') {
    $VERSION = $matches[1]
    Write-Host "Starting deployment for version: $VERSION" -ForegroundColor Green

    Write-Host "1/3 Compiling Java code..." -ForegroundColor Cyan
    .\mvnw clean package -DskipTests

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Compiling Java code failed!" -ForegroundColor Red
        exit 1
    }

    Write-Host "2/3 Building Docker image..." -ForegroundColor Cyan
    docker build -t justasn8/job-scraper:$VERSION -t justasn8/job-scraper:latest .

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Docker build failed! Is Docker Desktop running?" -ForegroundColor Red
        exit 1
    }

    Write-Host "3/3 Pushing image to Docker Hub..." -ForegroundColor Cyan

    docker push justasn8/job-scraper:$VERSION
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to push $VERSION to Docker Hub!" -ForegroundColor Red
        exit 1
    }

    docker push justasn8/job-scraper:latest
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to push 'latest' to Docker Hub!" -ForegroundColor Red
        exit 1
    }

    Write-Host "Deployment finished! You can now apply Terraform/Terragrunt." -ForegroundColor Green
} else {
    Write-Host "Error: Could not find 'tag:' variable in values-prod.yaml" -ForegroundColor Red
}