# Sequence Chart TSN Extension
This fork of the OMNeT++ Sequence Chart add TSN specific visuals to the vector attach to axis menu. Currently TSN egress queue fill and gate states are the only added visuals. See the below paper (coming soon!) for more info.

# Usage
First, you need to have created and run a TSN simulation in OMNeT++.
- After running the simulation (and calling `finish()` on all the modules), open the eventlog in the `results/` folder (if the eventlog file is not there, make sure you have `record-eventlog = true` in your omnetpp.ini).
- For the specific tsnSwitch Ethernet interface you wish to add visuals to, right click on its axis and select `Attach vector to axis`
- Select the vector file (ends in .vec) for your TSN simulation run
- Search for `gateState` in the vectors search box
- Select any of the `gateState` vectors emitted by the Ethernet interface you are interested in, the plugin should automically display the schedule and queue fill in the axis

# Example
Below is an example from the above paper. The input network is a simple client-server network shown in (a). Frames were sent from the client module every 500 microseconds to the server module, and from the server to the client every 2000 microseconds. All traffic has the same traffic class, and synchronization via gPTP is omitted for simplicity. The switch module’s Ethernet interfaces open the gate (written as GATE 0 in (c)) that controls the one traffic class’s flow for 1 millisecond and then loses it for 2 milliseconds.

![Before and After TSN visuals on a simple network](https://raw.githubusercontent.com/gormae1/SequenceChartTSN-ify/refs/heads/main/before_and_after_example.png)

- Part (b) of Figure 1 shows a snippet of the resulting Sequence Chart without TSN visuals. All events are filtered out except for the frames sent between client and server (displayed as pcp0:{frame number}), and the timer controlling the gate (displayed as ChangeTimer). The modules/axes shown from top to bottom are: the client module, the switch Ethernet interface connected to the client (switch.eth[0]), the switch Ethernet interface connected to the server (switch.eth[1]), and the server module. The ChangeTimer is a self-message clock event that is sent and received at each opening or closing of the gate, and by default the only insight into the state of the gate. 

- Finally, part (c) of Figure 1 shows the sequence chart with our TSN visuals. The chart has the same filtering as (b) (except for the ChangeTimer events), and the order of the modules is kept. The schedule for GATE 0 is drawn on the axis for switch.eth[0] and switch.eth[1]. The solid red component of the rectangular schedule indicates that the gate is closed, and the green component indicates that it is open. The varying blue line is the queue fill — the line increases in verticality the more a queue is populated. Since there is no specified limit to queue length, the queue fill line is linearly interpolated to the height of the rectangle, so a line at the bottom of the rectangle indicates an empty queue and a line at the top indicates that the queue fill is at its highest for the simulation run.
