function isHotfixOf(hotfixVersion, parentVersion) {
    return hotfixVersion.startsWith(parentVersion + '-') ||
           hotfixVersion.startsWith(parentVersion + '.')
}

export function groupHotfixVersions(filteredVersions) {
    const regularVersions = filteredVersions.filter(version => !version.hotfix)
    const hotfixVersions = filteredVersions.filter(version => version.hotfix)

    hotfixVersions.forEach(hotfixVersion => {
        const parentVersion = regularVersions.find(version =>
            isHotfixOf(hotfixVersion.version, version.version)
        )
        if (!parentVersion) {
            console.warn(`Parent version not found for hotfix version ${hotfixVersion.version}`)
            return
        }
        if (!parentVersion.childNodes) {
            parentVersion.childNodes = []
            parentVersion.isExpanded = true
        }
        parentVersion.childNodes.push(hotfixVersion)
    })

    return regularVersions
}