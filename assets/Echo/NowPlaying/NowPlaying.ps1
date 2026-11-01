Add-Type -AssemblyName System.Runtime.WindowsRuntime

try {
    $mgr = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync().GetAwaiter().GetResult()
    $session = $mgr.GetCurrentSession()

    if ($null -eq $session) {
        exit 0
    }

    $props = $session.TryGetMediaPropertiesAsync().GetAwaiter().GetResult()

    if ([string]::IsNullOrWhiteSpace($props.Title)) {
        exit 0
    }

    Write-Output "$($props.Artist)|$($props.Title)"
}
catch {
    exit 0
}