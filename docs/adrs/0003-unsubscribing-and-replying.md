# Unsubscribing and Replying

Date: 2023-09-14

## Status

Accepted

## Context

Message recipients must legally be allowed to remove consent (referred to as unsubscribing) 
for both email and SMS messages. To honor unsubscribe actions, this application stores consent status 
in order to filter out messages when appropriate. 

## Decision

Messages will always be sent unless there is a relevant `unsubscribed` record.

Each unsubscribe or subscribe action is recorded along with the phone number or email address it came with. 
No other information (such as a name or ID number) is stored with this consent. 


### Email
Unsubscribing from emails is done by following a link, which is added by default in a message at the 
end of every email. Once unsubscribed, a manual change is required on the backend to subscribe again. 
Inbound emails from recipients are not captured or recorded.

### SMS
Unsubscribing from SMS messages is done by sending a message saying only "STOP" to the account sending 
the messages. This default keyword is set by industry standards and cannot be changed. Unsubscribe 
instructions are not included in messages by default and must be intentionally put in the templates 
when applicable. Once an unsubscribe message is recorded, then a single confirmation message may be sent 
with instructions to re-subscribe.

(Re-)subscribing is available to recipients by sending a message saying only "START". This default 
keyword is set by industry standards and cannot be changed. Subscribe instructions can be provided 
in the confirmation message when unsubscribing. 

All inbound messages are ingested and examined for keywords. Non-keyword messages are ignored and 
not stored.


## Consequences

* When the same phone number is used by multiple recipients then an unsubscribe request will stop 
all messages to that number
* If a recipient unsubscribes and then changes their contact info, they will have to
unsubscribe again from that new email or phone number
