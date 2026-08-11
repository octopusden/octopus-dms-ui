import {Component} from 'react'
import meta from "./presenter.jsx";
import get from "lodash/get";
import {connect} from "react-redux";

const mapStateToProps = (state) => {
    const {meta} = get(state, "components.currentArtifacts")
    // info.links.jira-base-url, carried on the /actuator/info payload already in buildInfo.
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