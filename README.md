##Scotland Yard
###Overview
This is the README file for our ScotlandYard work.
We can assign each other work and update each other on our progress here as well as making a detailed log of what's been done in each session to make our final report/presentation easier.

###Patterns used
1. Visitor pattern with dynamic dispatch - in order to distinguish between different types of moves in the accept method.


###Fine tuning 
1. Encapsulation


###Recurring failed tests and problems
1. ~~testMrXMissingAnyTicketShouldThrow~~
* _Null pointer was solved by rearranging order in checkTickets(). Initialised first_
2. ~~Initialising player colour list seems to regress other tests~~
3. ~~testCallbackWithNullWillThrow~~
4. ~~testRoundWaitsForPlayerWhoDoesNotRespond~~
5. ~~validMove doubles tests~~
* _requestMove() called unnecessarily_
* added return after Mr. X moved so it extra calls weren't made. This passed 21 tests as it faulted rotation logic.
6. ~~isGameOver tests not passing~~
* _requestMove() called unnecessarily_
7. ~~OutofBoundsError and RevealRoound not working~~
* Index for this.rounds needed to be -1 of the actual round
8. ~~Detectives list not giving desired results~~
* this.detectives was first not being edited along with the game and then was not a reference to this.players rather than a copy of it. Solved by adding 'new ArrayList(this.players)'.
9. ~~Winning players list turned out immutable~~
* cannot edit emptyset. Had to create a new set and then make it empty at the end if no one had won.

###Work assignment
* Explore all RFIs and see if you can use more patterns 
* Mark all functions with proper documentation. Compare this with official notes used in lectures.


###LOG
* READING WEEK: repository created. Read over work briefly.
* 06/03 LAB: 9T. Rounds and graphs initialised non null and non empty. Mr. X made non black.
* HOME: 14T. Pre-existing issues resolved. Configured players and added locations (making sure of no duplicates).
* 10/03 LAB: Neither of us went.
* HOME: 17T. Checked whether detectives do not have secret and double tickets.
* 13/03 LAB: 20T. Made sure tickets not null. Created list of ScotlandYardPlayers. getRounds implemented. Pre-existing issues resolved.
* HOME: 30T. DRY code removed and Model class split into smaller methods. getGraph implemented.isGameOver set to false; getPlayerLocation set to 0. getPlayerTickets implemented. getPlayers fixed - playersCol was not created properly. getWinningPlayers half implemented with empty, unmodifiable set.
* HOME2: 34T. Fixed order of configs. Implemented getCurrentPlayer using get in our array list and a 'pointer' to currentPlayer. Took out "throw exception" which was throwing incorrectly.
* HOME3: 37T. Set up startRotate and made sure moves supplied not in 'validMoves' threw.
* 17/03 LAB: Neither of us went.
* 20/03 LAB: Began validMoves function.
* HOME: 39T. TicketMove implemented. Included SecretMove and DoubleMove but problems occured with null pointers again.
* 24/03 LAB: Null pointer identified.
* HOME: 46T. Problems with null pointers solved. Caused several NullPointerExceptions to be solved. Due to not initialising currentPlayer.
* 27/03 LAB: Attempted to implement DoubleMove validMoves unsuccessfully.
* HOME: 51T. Refined validMoves function. isRevealRound implemented. isGameOver and getWinningPlayers partially implemented.Spectators initialised and (un)registerSpectators() implemented. Players notified.
* HOME2: 52T. Extended accept method to start visit pattern. Detective ticket test finally passed due to incorrect ordering - Null should have been checked before anything else.
* HOME3: 54T. Improved encapsulation and removed explicit types where necessary. getSpectators implemented. Visitor method implemented to apply move logic.
* HOME4: 50T(regress). Added requestMove() to accept method. Made round tests pass but spectator tests fail.
* 31/03 LAB: Neither of us went.
* 04/04 HOME: 63T. Commented out large sections in order to redo bits. Register and unregister spectators now implemented properly with all test regarding these passed. validMoves() now implemented properly using test information and print statements to debug. Ticket decremented partially implemented.
* 10/04 HOME: 91T. Attempted to implement spectator notifications. Added 'return' at end of Mr. X move (21 tests passed). Removed an extra onMoveMade (4 tests passed). Mr. X ticket addition implemented. Removed getter side effects.
* 11/04 HOME: 99T. Mr. X's hidden rounds implemented. Spectators now notified only with Mr. X's revealed location. 
* 12/04 HOME: 114T. Fixed this.detectives problem. Allowed isGameOver() logic to work. WinningPlayers fixed. isGameOver now called at correct time with spectator notifications. Rearranged rotation logic to pass all but one test. Added new test.
* 24/04 LAB: 115T. Fixed last test. Round incremented at wrong time. All tests passed! Got GUI working by making sure GameOver isn't checked before anyone has moved.