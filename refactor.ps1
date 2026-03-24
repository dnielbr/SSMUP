$ErrorActionPreference = "Stop"

$basePath = "c:\Users\dnbs\IdeaProjects\SSMUP\src\main\java\com\br\ssmup"
$testBasePath = "c:\Users\dnbs\IdeaProjects\SSMUP\src\test\java\com\br\ssmup"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

function ReplaceInFiles($oldStr, $newStr) {
    Write-Host "    Replacing '$oldStr' with '$newStr' in all .java files..."
    Get-ChildItem -Path "c:\Users\dnbs\IdeaProjects\SSMUP\src" -Recurse -Filter "*.java" | ForEach-Object {
        $content = [System.IO.File]::ReadAllText($_.FullName)
        if ($content.Contains($oldStr)) {
            $content = $content.Replace($oldStr, $newStr)
            [System.IO.File]::WriteAllText($_.FullName, $content, $utf8NoBom)
        }
    }
}

# 1. Módulo Usuario -> Auth
Write-Host "1. Movendo usuario para auth..."
if (Test-Path "$basePath\usuario") { Move-Item -Path "$basePath\usuario" -Destination "$basePath\auth\usuario" -Force }
if (Test-Path "$testBasePath\usuario") { Move-Item -Path "$testBasePath\usuario" -Destination "$testBasePath\auth\usuario" -Force }
ReplaceInFiles "com.br.ssmup.usuario" "com.br.ssmup.auth.usuario"

# 2. Empresa Core -> Empresa/Cadastro
Write-Host "2. Movendo Empresa Core para Cadastro..."
$empresaSubdirs = @("controller", "dto", "entity", "mapper", "repository", "service", "solr", "specification")
if (-Not (Test-Path "$basePath\empresa\cadastro")) { New-Item -ItemType Directory -Force -Path "$basePath\empresa\cadastro" | Out-Null }
if (-Not (Test-Path "$testBasePath\empresa\cadastro")) { New-Item -ItemType Directory -Force -Path "$testBasePath\empresa\cadastro" | Out-Null }

foreach ($dir in $empresaSubdirs) {
    if (Test-Path "$basePath\empresa\$dir") {
        Move-Item -Path "$basePath\empresa\$dir" -Destination "$basePath\empresa\cadastro\" -Force
    }
    if (Test-Path "$testBasePath\empresa\$dir") {
        Move-Item -Path "$testBasePath\empresa\$dir" -Destination "$testBasePath\empresa\cadastro\" -Force
    }
    ReplaceInFiles "com.br.ssmup.empresa.$dir" "com.br.ssmup.empresa.cadastro.$dir"
}

# 3. Demais módulos -> Empresa
$otherModules = @("cnae", "endereco", "historico", "inspecao", "licensa", "responsavel")
foreach ($mod in $otherModules) {
    Write-Host "3. Movendo $mod para empresa..."
    if (Test-Path "$basePath\$mod") {
        Move-Item -Path "$basePath\$mod" -Destination "$basePath\empresa\" -Force
    }
    if (Test-Path "$testBasePath\$mod") {
        Move-Item -Path "$testBasePath\$mod" -Destination "$testBasePath\empresa\" -Force
    }
    ReplaceInFiles "com.br.ssmup.$mod" "com.br.ssmup.empresa.$mod"
}

Write-Host "Refatoração de pastas e imports concluída com sucesso."
