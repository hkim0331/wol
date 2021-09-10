all: install

install:
#	cp wol.edn ${HOME}/clojure/
#	cp wol.clj ${HOME}/dotfiles/utils/
	install -m 0700 wol.clj ${HOME}/bin/
