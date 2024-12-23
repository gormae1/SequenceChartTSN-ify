# Sequence Chart TSN Extension
This fork of the OMNeT++ Sequence Chart add TSN specific visuals to the vector attach to axis menu. Currently TSN egress queue fill and gate states are the only added visuals. See the below paper (coming soon) for more info.

# Usage
First, you need to have created and run a TSN simulation in OMNeT++.
- After running the simulation (and calling `finish()` on all the modules), open the eventlog in the `results/` folder (if the eventlog file is not there, make sure you have `record-eventlog = true` in your omnetpp.ini).
- For the specific tsnSwitch Ethernet interface you wish to add visuals to, right click on its axis and select `Attach vector to axis`
- Select the vector file (ends in .vec) for your TSN simulation run
- Search for `gateState` in the vectors search box
- Select any of the `gateState` vectors emitted by the Ethernet interface you are interested in, the plugin should automically display the schedule and queue fill in the axis

# Example
Coming soon.
