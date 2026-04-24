$maxConcurrent = 20
$jobs = @()

foreach ($i in 1..200) {
    $jobs += Start-Job -ScriptBlock {
        param($i)

        $body = @{
            authorId   = $i
            authorType = "BOT"
            content    = "Spam $i"
            depthLevel = 1
        } | ConvertTo-Json -Depth 3

        Invoke-RestMethod -Uri "http://localhost:8080/api/posts/1/comments" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body
    } -ArgumentList $i

    # Limit concurrency
    while (($jobs | Where-Object { $_.State -eq "Running" }).Count -ge $maxConcurrent) {
        Start-Sleep -Milliseconds 200
    }
}

# Wait and collect results
$jobs | ForEach-Object { Receive-Job $_ -Wait }