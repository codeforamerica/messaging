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
Unsubscribing from emails is done by following a link, utilizing Mailgun's unsubscribe feature: they 
host an endpoint for unsubscribes and provide a URL template variable substitution feature. This URL 
is added by default at the end of every email message with localized instructions set with 
`email.unsubscribe.footer`.

Once unsubscribed, a manual change is required on the backend to subscribe again. Inbound emails 
from recipients are not captured or recorded.

### SMS
Unsubscribing from SMS messages is done by sending a message saying only "STOP" or a configured synonym 
to the account sending the messages (STOP is a default keyword is set by industry standards and cannot be 
changed). Twilio provides Advanced Opt-Out Management that allows for configuration of keywords and 
corresponding auto-responses. Inbound messages through Twilio will show an `OptOutType` to indicate 
keyword messages (Note: not only opt-out, also opt-in and HELP).

Unsubscribe instructions are not included in SMS messages by default and must be intentionally put in 
the templates when applicable. Once an unsubscribe message is recorded, then a single confirmation 
message may be sent with instructions to re-subscribe (configurable in Twilio).

(Re-)subscribing is available to recipients by sending a message saying only "START" or a configured 
synonym (START is a default keyword set by industry standards and cannot be changed). Subscribe 
instructions can be provided in the confirmation message when unsubscribing. 

All inbound SMS messages are ingested and examined for keywords. Non-keyword messages are ignored 
and not stored.


## Consequences

* When the same phone number or email is used by multiple recipients then an unsubscribe request 
will stop all messages to that phone number or email.
* If a recipient unsubscribes and then changes their contact info, they will have to
unsubscribe again from that new email or phone number.
* Mailgun's unsubscribe endpoint only supports English. The unsubscribe instructions, however, will 
be localized.
