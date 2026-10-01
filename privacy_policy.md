# Privacy Policy

Welcome to the Cubench app (formerly Nano Timer) for Android!

This is a free and open source Android app developed by Nicolas Aubinet. The source code is available on GitHub under the GNU GPL v3.0 license. The app is also available on Google Play.

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to collect any personally identifiable information. The data you create (your solves, times and settings) is stored on your device, and can be erased by clearing the app's data or uninstalling it. The one exception is the coach, described below, and it sends nothing until you turn weekly plans on.

# The coach

The coach reads your solve statistics and writes you a practice plan every week. It runs on a server, not on your phone, so it is the one part of the app that sends data over the internet. Nothing is sent until you turn weekly plans on, and the app asks you first, naming the solve type the plans are written for.

**When it is sent.** Once weekly plans are on, the app sends a summary when a new plan is due: at most once a week, in the background, without you asking. It also sends the same summary when you open the Plan tab, to refresh the practice sets shown under your plan.

**What is sent.** A summary of one solve type's statistics: how long each step and each last layer case takes you on average, how many moves and pauses each step takes, how often, which algorithms you already know, and the same figures for your drills. From the second plan on it also carries what your last plan asked you to work on and how many solves and drill repetitions you have done since. It holds averages, bests and counts, not your individual solves or scrambles, and never any text you typed, such as the names you gave your solve types. No date or time is sent. Alongside it go a random identifier for that solve type, so the coach can tell your plans apart, and an access token that identifies your subscription.

**Who processes it.** The server runs on Google Cloud. To write your plan, the summary is sent to Anthropic, whose Claude model writes it. Anthropic processes it on my behalf and does not use it to train its models.

**What is kept.** The server keeps each summary and the plan written from it, linked to your access token, so it can limit how many plans are written per week and avoid charging you twice for the same request. Both are deleted two weeks after the plan is written. Your IP address is used to limit how often requests can be made and is deleted within a day; the hosting provider's own request logs may also hold it for a short time.

**Figures shared between users.** To tell whether a case is slow for you or slow for everyone, the server combines every user's latest summary into one average per case. Only the combined averages are kept, so no stored figure can be traced back to you.

**Notifications.** The app tells you when a new plan is ready and, once per plan, how many solves the next one still needs. Both are written on your phone and nothing is sent to show them. The reminder can be turned off in the settings.

**Payments.** A subscription is bought through Google Play. I never see your payment details.

**Deleting your data.** Email me at the address below and I will delete everything the server holds for your access token.

# Contact Me

If you have any questions about this Privacy Policy, you can contact me:

- By email: nanotimercube@gmail.com

Last updated: [set on the day the coach is released]
