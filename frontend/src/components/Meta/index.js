import {Component} from 'react'
import meta from "./presenter.jsx";
import get from "lodash/get";
import {connect} from "react-redux";

const mapStateToProps = (state) => {
    const {meta} = get(state, "components.currentArtifacts")
    // Resolved at runtime from info.links.jira-base-url in the config server, and
    // surfaced on /actuator/info - the same payload the footer already reads for the
    // build version. Kept out of the bundle so one image works in every environment.
    const jiraBaseUrl = get(state, "components.buildInfo.links.jira-base-url")
    return {
        meta, jiraBaseUrl
    }
}

const mapDispatchToProps = (dispatch) => {
    return {}
}

const mergeProps = (stateProps, dispatchProps, ownProps) => {
    return {
        ...stateProps,
        ...dispatchProps,
        ...ownProps
    }
}

class Meta extends Component {
    render() {
        return meta(this.props)
    }
}

export default connect(mapStateToProps, mapDispatchToProps, mergeProps)(Meta)