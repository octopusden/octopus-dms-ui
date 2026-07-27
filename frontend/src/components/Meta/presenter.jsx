import React from "react";
import './style.css'
import {Icon} from "@blueprintjs/core";

// Base URL of the OWS Jira instance, used to linkify issue keys in the release
// limitations text. This mirrors what the release-notification email already
// does server-side (MessageFormatter.postProcessLimitations), which resolves a
// single base URL from APKeys.JIRA_BASEURL and applies it to every key. The
// portal has no runtime config, so the same URL is pinned here.
const JIRA_BASE_URL = "https://ows-jira.spb.openwaygroup.com"
const JIRA_KEY_PATTERN = /([A-Z][A-Z_0-9]+-\d+)/g

export default function meta(props) {
    const {meta} = props
    if (meta.ready) {
        return <div className="meta-container">
            <div className="meta-wrapper">
                <div className="meta-column">
                    <MetaItem icon='application' keyName='Component name' value={meta.componentName}/>
                    <MetaItem icon='id-number' keyName='Component ID' value={meta.componentId}/>
                    <MetaItem icon='box' keyName='Version' value={meta.version}/>
                </div>
                <div className="meta-column">
                    <MetaItem icon='wrench' keyName='Hotfix' value={meta.hotfix ? "yes" : "no"}/>
                    <MetaItem icon='applications' keyName='Solution' value={meta.solution ? "yes" : "no"}/>
                    <MetaItem icon='git-push' keyName='Published' value={meta.published ? "yes" : "no"}/>
                </div>
                <div className="meta-column">
                    <MetaItem icon='dollar' keyName='Client Code' value={!!meta.clientCode ? meta.clientCode : "none"}/>
                    <MetaItem icon='fork' keyName='Parent Component ID' value={!!meta.parentComponent ? meta.parentComponent : "none"}/>
                    <MetaItem icon='build' keyName='Status' value={meta.status}/>
                </div>
            </div>
            <MetaLimitations value={meta.limitations}/>
        </div>
    } else {
        return <div className="meta-wrapper"></div>
    }
}

function MetaItem(props) {
    const {icon, keyName, value} = props
    return <div className="meta-item">
        <Icon className="meta-icon" icon={icon} iconSize={12}/>
        <strong className="meta-item-key">{keyName}:</strong>
        <div className="meta-item-value">{value}</div>
    </div>
}

function MetaLimitations(props) {
    const {value} = props
    if (!value || !value.trim()) {
        return null
    }
    return <div className="meta-limitations">
        <div className="meta-limitations-key">
            <Icon className="meta-icon" icon='warning-sign' iconSize={12}/>
            <strong>Release Limitations:</strong>
        </div>
        <div className="meta-limitations-value">{linkifyJiraKeys(value)}</div>
    </div>
}

// Splits the raw text on Jira issue keys and turns each key into a link to the
// ticket. Returns an array of strings and anchors for React to render - the text
// itself stays escaped by React, so it is never interpreted as markup.
function linkifyJiraKeys(text) {
    return text.split(JIRA_KEY_PATTERN).map((part, index) =>
        // split() with a capturing group puts the captured keys at the odd indices.
        index % 2 === 1
            ? <a key={index} href={`${JIRA_BASE_URL}/browse/${part}`} target="_blank" rel="noopener noreferrer">{part}</a>
            : part
    )
}
