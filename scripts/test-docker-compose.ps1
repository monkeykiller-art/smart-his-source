$ErrorActionPreference = 'Stop'

$repository = Split-Path -Parent $PSScriptRoot
$composePath = Join-Path $repository 'docker-compose.yml'
$content = [IO.File]::ReadAllText($composePath)

$requiredFragments = @(
    'image: apache/kafka:3.9.2',
    'KAFKA_NODE_ID:',
    'KAFKA_PROCESS_ROLES:',
    'KAFKA_CONTROLLER_QUORUM_VOTERS:',
    'KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR:',
    '/opt/kafka/bin/kafka-topics.sh'
)
foreach ($fragment in $requiredFragments) {
    if (-not $content.Contains($fragment)) {
        throw "docker-compose.yml is missing required Kafka configuration: $fragment"
    }
}

foreach ($obsolete in @('bitnami/kafka', 'KAFKA_CFG_', 'ALLOW_PLAINTEXT_LISTENER')) {
    if ($content.Contains($obsolete)) {
        throw "docker-compose.yml still contains obsolete Bitnami configuration: $obsolete"
    }
}

$previous = @{}
$previousDockerConfig = [Environment]::GetEnvironmentVariable('DOCKER_CONFIG', 'Process')
$values = @{
    POSTGRES_PASSWORD = 'compose-validation-password'
    NACOS_AUTH_TOKEN = 'Y29tcG9zZS12YWxpZGF0aW9uLXRva2VuLXdpdGgtMzItYnl0ZXM='
    NACOS_AUTH_IDENTITY_KEY = 'compose-validation-key'
    NACOS_AUTH_IDENTITY_VALUE = 'compose-validation-value'
}
foreach ($name in $values.Keys) {
    $previous[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
    [Environment]::SetEnvironmentVariable($name, $values[$name], 'Process')
}
[Environment]::SetEnvironmentVariable(
    'DOCKER_CONFIG',
    (Join-Path ([IO.Path]::GetTempPath()) 'smart-his-compose-validation'),
    'Process'
)
New-Item -ItemType Directory -Force -Path $env:DOCKER_CONFIG | Out-Null

try {
    & docker compose --file $composePath config --quiet
    if ($LASTEXITCODE -ne 0) { throw 'docker compose config validation failed.' }
} finally {
    foreach ($name in $values.Keys) {
        [Environment]::SetEnvironmentVariable($name, $previous[$name], 'Process')
    }
    [Environment]::SetEnvironmentVariable('DOCKER_CONFIG', $previousDockerConfig, 'Process')
}

Write-Output 'docker-compose.yml validation passed.'
