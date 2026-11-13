# Resolve assistant materials by category; final installers stay in the checkout.
function Get-ProjectMaterialsRoot {
    param([Parameter(Mandatory = $true)][string]$ProjectRoot,
          [ValidateSet('Forme', 'Workspace', 'Snapshots', 'Trash')][string]$Category = 'Workspace')
    $source = [IO.Path]::GetFullPath($ProjectRoot).TrimEnd('\', '/')
    $drive = if ([IO.Directory]::GetLogicalDrives() -contains 'D:\') { 'D:\' } else { 'C:\' }
    return [IO.Path]::Combine($drive, 'work', $Category, [IO.Path]::GetFileName($source))
}

function Join-ProjectMaterialPath {
    param([Parameter(Mandatory = $true, Position = 0)][string]$Path,
          [Parameter(Mandatory = $true, Position = 1)][string]$ChildPath)
    if ([IO.Path]::IsPathRooted($ChildPath)) {
        return [IO.Path]::GetFullPath($ChildPath)
    }
    $root = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/')
    $first = ($ChildPath -split '[\\/]')[0]
    if ($first -iin @('Forme', 'Workspace', 'snapshots', 'trash')) {
        $root = Get-ProjectMaterialsRoot -ProjectRoot $root -Category $first
        $ChildPath = $ChildPath.Substring($first.Length).TrimStart('\', '/')
    }
    $result = [IO.Path]::GetFullPath([IO.Path]::Combine($root, $ChildPath))
    if (-not $result.Equals($root, [StringComparison]::OrdinalIgnoreCase) -and
        -not $result.StartsWith($root + '\', [StringComparison]::OrdinalIgnoreCase)) {
        throw "Path escapes the permitted project area: $ChildPath"
    }
    return $result
}

function Test-ProjectMaterialPath {
    param([string]$Path, [string]$ProjectRoot)
    $full = [IO.Path]::GetFullPath($Path).TrimEnd('\', '/')
    $roots = @([IO.Path]::GetFullPath($ProjectRoot).TrimEnd('\', '/'))
    foreach ($category in @('Forme', 'Workspace', 'Snapshots', 'Trash')) {
        $roots += Get-ProjectMaterialsRoot -ProjectRoot $ProjectRoot -Category $category
    }
    foreach ($root in $roots) {
        if ($full.StartsWith($root + '\', [StringComparison]::OrdinalIgnoreCase)) { return $true }
    }
    return $false
}

# Build scripts supply the verified drive to MSBuild without a silent access fallback.
$env:ProjectMaterialsDrive = if ([IO.Directory]::GetLogicalDrives() -contains 'D:\') { 'D:' } else { 'C:' }
