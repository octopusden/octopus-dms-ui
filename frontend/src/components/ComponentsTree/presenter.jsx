import React from 'react'
import {Spinner, Tree} from "@blueprintjs/core";
import {getSecondaryLabel} from "../common";

const treeLevel = {
    ROOT: 'ROOT',
    MINOR: 'MINOR'
}

function componentsTree(props) {
    const {
        loadingComponents, handleNodeClick
    } = props

    const nodes = componentsToNodes(props)

    if (loadingComponents) {
        return <div className="load-components-wrapper">
            <Spinner size={50} intent="primary"/>
        </div>
    } else {
        return <div className="components-tree">
            <div className="components-tree-wrapper">
                <Tree
                    contents={nodes}
                    onNodeClick={handleNodeClick}
                    onNodeCollapse={handleNodeClick}
                    onNodeExpand={handleNodeClick}
                />
            </div>
        </div>
    }
}

function componentsToNodes(props) {
    const {components} = props
    return Object.values(components)
        .filter(component => {
            return props.searchQuery
                ?
                (component.id.toLowerCase().includes(props.searchQuery.toLowerCase())
                    ||
                    component.name.toLowerCase().includes(props.searchQuery.toLowerCase()))
                : true
        })
        .map(component => {
            let childNodes = []
            const componentId = component.id
            if (component.minorVersions) {
                childNodes = renderComponentMinorVersions(componentId, component.minorVersions, props)
            }

            return {
                id: componentId,
                level: treeLevel.ROOT,
                componentId: componentId,
                isExpanded: component.expand,
                label: component.name,
                icon: component.solution ? 'applications' : 'application',
                childNodes: childNodes,
                secondaryLabel: getSecondaryLabel(component)
            }
        })
}

function renderComponentMinorVersions(componentId, minorVersions, props) {
    return Object.values(minorVersions).map(minorVersion => {
        let childNodes = []
        const minorVersionId = minorVersion.id
        const versions = minorVersion.versions
        if (versions) {
            childNodes = renderComponentVersions(componentId, minorVersionId, versions, props)
        }
        return {
            level: treeLevel.MINOR,
            id: minorVersionId,
            label: minorVersionId,
            version: minorVersionId,
            componentId: componentId,
            icon: 'filter',
            isExpanded: minorVersion.expand,
            childNodes: childNodes,
            secondaryLabel: getSecondaryLabel(minorVersion)
        }
    })
}

function renderComponentVersions(componentId, minorVersionId, versions, props) {
    const {showRc, currentArtifacts} = props
    const {selectedComponent, selectedVersion} = currentArtifacts

    const filteredVersions = Object.values(versions).filter(version => {
        return showRc || version.status !== 'RC'
    })

    const result = []
    let i = 0

    while (i < filteredVersions.length) {
        const version = filteredVersions[i]
        const versionId = version.version
        const displayName = versionId + (version.status === 'RELEASE' ? '' : `-${version.status}`)

        if (componentId == "test-component-external") {
            if (version.version.includes("-")) {
                const hotfixVersions = []
                let j = i
    
                while (j < filteredVersions.length && filteredVersions[j].version.includes("-")) {
                    hotfixVersions.push(filteredVersions[j])
                    j++
                }
    
                if (j < filteredVersions.length) {
                    const parentVersion = filteredVersions[j]
                    const parentVersionId = parentVersion.version
    
                    const allHotfixesMatchParent = hotfixVersions.every(hf =>
                        hf.version.startsWith(parentVersionId)
                    )
    
                    if (allHotfixesMatchParent) {
                        const parentDisplayName = parentVersionId + (parentVersion.status === 'RELEASE' ? '' : `-${parentVersion.status}`)
    
                        const childNodes = hotfixVersions.map(hf => {
                            const hfVersionId = hf.version
                            const hfDisplayName = hfVersionId + (hf.status === 'RELEASE' ? '' : `-${hf.status}`)
                            return {
                                id: hfVersionId,
                                label: hfDisplayName,
                                version: hfVersionId,
                                minorVersion: minorVersionId,
                                componentId: componentId,
                                icon: 'wrench',
                                isSelected: selectedComponent === componentId && selectedVersion === hfVersionId
                            }
                        })

                        const parentNode = {
                            id: parentVersionId,
                            label: parentDisplayName,
                            version: parentVersionId,
                            minorVersion: minorVersionId,
                            componentId: componentId,
                            icon: 'build',
                            isSelected: selectedComponent === componentId && selectedVersion === parentVersionId
                        }
    
                        if (childNodes.length > 0) {
                            parentNode.childNodes = childNodes
                            parentNode.isExpanded = true
                        }
                        
                        result.push(parentNode)
    
                        i = j + 1
                        continue
                    }
                }
    
                i = j
                continue
            }
        }


        result.push({
            id: versionId,
            label: displayName,
            version: versionId,
            minorVersion: minorVersionId,
            componentId: componentId,
            icon: 'build',
            isSelected: selectedComponent === componentId && selectedVersion === versionId
        })
        i++
    }

    return result
}


export {
    componentsTree,
    treeLevel
}