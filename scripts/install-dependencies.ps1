$ErrorActionPreference = 'Stop'

$userProfileDirectory = [Environment]::GetFolderPath('UserProfile')
$mavenRepository = Join-Path $userProfileDirectory '.m2\repository\com\fasterxml\jackson\core'
$dependencies = @(
    @{
        Directory = '..\..\..\zaxxer\HikariCP\7.0.2'
        File = 'HikariCP-7.0.2.jar'
        Url = 'https://repo.maven.apache.org/maven2/com/zaxxer/HikariCP/7.0.2/HikariCP-7.0.2.jar'
    },
    @{
        Directory = '..\..\..\..\org\slf4j\slf4j-api\2.0.18'
        File = 'slf4j-api-2.0.18.jar'
        Url = 'https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/2.0.18/slf4j-api-2.0.18.jar'
    },
    @{
        Directory = '..\..\..\..\org\slf4j\slf4j-nop\2.0.18'
        File = 'slf4j-nop-2.0.18.jar'
        Url = 'https://repo.maven.apache.org/maven2/org/slf4j/slf4j-nop/2.0.18/slf4j-nop-2.0.18.jar'
    },
    @{
        Directory = '..\..\..\..\org\junit\platform\junit-platform-launcher\1.11.4'
        File = 'junit-platform-launcher-1.11.4.jar'
        Url = 'https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.11.4/junit-platform-launcher-1.11.4.jar'
    },
    @{
        Directory = 'jackson-annotations\2.21'
        File = 'jackson-annotations-2.21.jar'
        Url = 'https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.21/jackson-annotations-2.21.jar'
    },
    @{
        Directory = 'jackson-core\2.21.6'
        File = 'jackson-core-2.21.6.jar'
        Url = 'https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.21.6/jackson-core-2.21.6.jar'
    },
    @{
        Directory = 'jackson-databind\2.21.6'
        File = 'jackson-databind-2.21.6.jar'
        Url = 'https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.21.6/jackson-databind-2.21.6.jar'
    }
)

foreach ($dependency in $dependencies) {
    $directory = Join-Path $mavenRepository $dependency.Directory
    $target = Join-Path $directory $dependency.File
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    if (-not (Test-Path -LiteralPath $target)) {
        Invoke-WebRequest -Uri $dependency.Url -OutFile $target
    }
}

$postgresDirectory = Join-Path $userProfileDirectory '.m2\repository\org\postgresql\postgresql\42.7.13'
$postgresTarget = Join-Path $postgresDirectory 'postgresql-42.7.13.jar'
New-Item -ItemType Directory -Force -Path $postgresDirectory | Out-Null
if (-not (Test-Path -LiteralPath $postgresTarget)) {
    Invoke-WebRequest `
        -Uri 'https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.13/postgresql-42.7.13.jar' `
        -OutFile $postgresTarget
}

Write-Host 'Dependências do projeto instaladas.'
