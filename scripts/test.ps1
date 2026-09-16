$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$testOutputDirectory = Join-Path $projectDirectory 'target\test-classes'
$userProfileDirectory = [Environment]::GetFolderPath('UserProfile')

& (Join-Path $PSScriptRoot 'compile.ps1')

$javaExtension = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.vscode\extensions') -Directory |
    Where-Object { $_.Name -like 'redhat.java-*-win32-x64' } |
    Sort-Object Name -Descending |
    Select-Object -First 1
$javac = Get-ChildItem -LiteralPath $javaExtension.FullName -Recurse -Filter 'javac.exe' |
    Select-Object -First 1 -ExpandProperty FullName
$java = Join-Path (Split-Path -Parent $javac) 'java.exe'

$dependencyRoots = @(
    (Join-Path $userProfileDirectory '.m2\repository\org\junit'),
    (Join-Path $userProfileDirectory '.m2\repository\org\opentest4j'),
    (Join-Path $userProfileDirectory '.m2\repository\org\apiguardian'),
    (Join-Path $userProfileDirectory '.m2\repository\com\fasterxml\jackson\core'),
    (Join-Path $userProfileDirectory '.m2\repository\com\zaxxer'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j')
)
$jars = $dependencyRoots |
    Where-Object { Test-Path -LiteralPath $_ } |
    ForEach-Object { Get-ChildItem -LiteralPath $_ -Recurse -Filter '*.jar' } |
    Where-Object { $_.FullName -match '(5\.11\.4|1\.11\.4|1\.3\.0|1\.1\.2|2\.21\.6|2\.21|7\.0\.2|2\.0\.18)' } |
    Select-Object -ExpandProperty FullName
$classPath = (@((Join-Path $projectDirectory 'target\classes')) + $jars) -join [IO.Path]::PathSeparator

$resolvedProjectDirectory = [IO.Path]::GetFullPath($projectDirectory)
$resolvedTestOutputDirectory = [IO.Path]::GetFullPath($testOutputDirectory)
if (-not $resolvedTestOutputDirectory.StartsWith(
        $resolvedProjectDirectory + [IO.Path]::DirectorySeparatorChar,
        [StringComparison]::OrdinalIgnoreCase
    )) {
    throw 'O diretório de testes está fora do projeto.'
}
if (Test-Path -LiteralPath $resolvedTestOutputDirectory) {
    Remove-Item -LiteralPath $resolvedTestOutputDirectory -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $testOutputDirectory | Out-Null
$testSources = Get-ChildItem -LiteralPath (Join-Path $projectDirectory 'test') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName
$launcherSource = Join-Path $PSScriptRoot 'LocalTestLauncher.java'

& $javac -encoding UTF-8 -cp $classPath -d $testOutputDirectory $testSources $launcherSource
if ($LASTEXITCODE -ne 0) {
    throw "A compilação dos testes falhou com o código $LASTEXITCODE."
}

$runtimeClassPath = (@($classPath, $testOutputDirectory)) -join [IO.Path]::PathSeparator
& $java -cp $runtimeClassPath LocalTestLauncher $testOutputDirectory
if ($LASTEXITCODE -ne 0) {
    throw "Os testes falharam com o código $LASTEXITCODE."
}
