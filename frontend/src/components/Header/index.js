import {Component} from 'react'
import header from "./presenter.jsx";
import {connect} from "react-redux";
import get from "lodash/get";

const mapStateToProps = (state) => {
    const username = get(state, "components.loggedUser.username")
    return {
        username
    }
}

const mapDispatchToProps = (dispatch) => {
    const logOut = async () => {
        try {
            await fetch('/logout', {
                method: 'POST',
                credentials: 'include'
            });
            window.location.href = '/';
        } catch (e) {
            console.error('Logout failed', e);
            window.location.href = '/';
        }
    }
    return { logOut }
}

const mergeProps = (stateProps, dispatchProps, ownProps) => {
    return {
        ...stateProps,
        ...dispatchProps,
        ...ownProps
    }
}

class Header extends Component {
    render() {
        return header(this.props)
    }
}

export default connect(mapStateToProps, mapDispatchToProps, mergeProps)(Header)