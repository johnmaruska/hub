(ns hub.discord.deck)

(bot/defextension deck [client message]
  (:init
   "Initialize a deck -- i.e. get a new playing card deck.")
  (:shuffle
   "Shuffle the deck. Keeps deck state w.r.t discards.")
  (:draw
   "Draw cards from the deck. Shows cards and removes them from the deck.")
  (:peek
   "Peek at cards from the deck. Shows cards and does not remove them form the deck."))
