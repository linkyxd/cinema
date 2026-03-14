# Run with TLS - project keystore (hide C:\Users\...\.keystore so Tomcat uses ours)
$keystore = "c:/javap/cinema/certs/cinema-keystore.p12"
$pass = "changeit"
$jar = "target/cinema-0.0.1-SNAPSHOT.jar"
$userKeystore = "$env:USERPROFILE\.keystore"
$userKeystoreBak = "$env:USERPROFILE\.keystore.bak.cinema"

if (-not (Test-Path $jar)) {
    Write-Host "Building JAR..."
    .\mvnw.cmd package -DskipTests -q
}
Remove-Item Env:JAVA_TOOL_OPTIONS -ErrorAction SilentlyContinue

# Temporarily rename user .keystore so Tomcat can't pick it
$restore = $false
if (Test-Path $userKeystore) {
    Rename-Item $userKeystore $userKeystoreBak -Force
    $restore = $true
    Write-Host "Temporarily hidden $userKeystore"
}

$javaArgs = @(
    "-Djavax.net.ssl.keyStore=$keystore",
    "-Djavax.net.ssl.keyStorePassword=$pass",
    "-Djavax.net.ssl.keyStoreType=PKCS12",
    "-Dserver.ssl.key-store=$keystore",
    "-Dserver.ssl.key-store-password=$pass",
    "-Dserver.ssl.key-store-type=PKCS12",
    "-Dserver.ssl.key-alias=cinema-server",
    "-jar", $jar,
    "--spring.profiles.active=tls"
)
try {
    & java $javaArgs
} finally {
    if ($restore -and (Test-Path $userKeystoreBak)) {
        Rename-Item $userKeystoreBak $userKeystore -Force
        Write-Host "Restored $userKeystore"
    }
}
