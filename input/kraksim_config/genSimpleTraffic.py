# -*- coding: utf-8 -*-

NUM_OF_CARS = 1000
NUM_OF_TURNS = 21600
NUM_OF_GATES = 10

print("""<?xml version="1.0"?>

<!--
	czas trwania schematu: 12h = 21600
	
	pomiędzy każdą parą węzłów generowane jest %d samochodów
	z rozkładem jednostajnym
-->
<traffic>
""" % (NUM_OF_CARS,))

for fromId in range(0,NUM_OF_GATES):
    for toId in range(0,NUM_OF_GATES):
        if fromId == toId:
            continue

        schemeStr = """
	<scheme count='%d'>
		<gateway id='%s'>
			<uniform a='0' b='%d' />
		</gateway>
		<gateway id='%s' />
	</scheme>
        """ % ( NUM_OF_CARS, "G"+str(fromId+1), NUM_OF_TURNS, "G"+str(toId+1) )

        print (schemeStr)

print ("</traffic>")
