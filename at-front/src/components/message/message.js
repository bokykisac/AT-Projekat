import React from 'react';
import axios from '../../axios';

class Message extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            msgContent: ''
        }
        this.msgAgentSender = React.createRef();
        this.msgPerformative = React.createRef();
        this.msgAgentReciever = React.createRef();
    }

    onSubmit = e => {
        e.preventDefault();

        const senderString = this.msgAgentSender.current.value.split("!");
        const senderName = senderString[0];
        const senderType = senderString[1];

        const recieverString = this.msgAgentReciever.current.value.split("!");
        const recieverName = recieverString[0];
        const recieverType = recieverString[1];

       const sendData = {
            performative: this.msgPerformative.current.value,
            content: this.state.msgContent,
            sender : senderName + "$localhost:8080$master$" + senderType + "$agents",
            receivers: [recieverName + "$localhost:8080$master$" + recieverType + "$agents"]
        }

        axios.post("rest/messages", sendData)
            .then(res => alert(res.data))
            .catch(err => console.log(err.response));
    }

    render() {
        let form;

        form = (
            <form onSubmit={this.onSubmit}>
                <div className="form-group">
                    <label htmlFor="msgAgentSender">Sender:</label>
                    <select className="form-control mb-2" id="msgAgentSender" ref={this.msgAgentSender}>
                        {this.props.runningAgents.map(agent => {
                            return <option key={agent.name} value={`${agent.name}!${agent.type.name}`}>{agent.name} ({agent.type.name})</option>
                        })}
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="msgPerformative">Performative:</label>
                    <select className="form-control mb-2" id="msgPerformative" ref={this.msgPerformative}>
                        {this.props.performatives.map(performative => {
                            return <option key={performative} value={performative}>{performative}</option>
                        })}
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="msgContent">Content:</label>
                    <input
                        type="text"
                        className="form-control"
                        id="msgContent"
                        placeholder="Message content"
                        value={this.state.msgContent}
                        onChange={e => this.setState({ msgContent: e.target.value })} />
                </div>
                <div className="form-group">
                    <label htmlFor="msgAgentReciever">Reciever:</label>
                    <select className="form-control mb-2" id="msgAgentReciever" ref={this.msgAgentReciever}>
                        {this.props.runningAgents.map(agent => {
                            return <option key={agent.name} value={`${agent.name}!${agent.type.name}`}>{agent.name} ({agent.type.name})</option>
                        })}
                    </select>
                </div>
                <button type="submit" className="btn btn-primary float-right">Submit</button>
            </form>
        )

        if (this.props.runningAgents.length < 1) {
            form = <p>To send messages, please start agents.</p>
        }

        return (
            <div className="container">
                {form}
            </div>
        )
    }
}

export default Message;