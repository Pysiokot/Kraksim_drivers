import csv
import matplotlib.pyplot as plt

x1 = []
y1 = []

x2 = []
y2 = []


with open('obstacles.csv', 'rt') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        x1.append(float(row[0]))
        y1.append(float(row[1]))

with open('noObstacles.csv', 'rt') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        x2.append(float(row[0]))
        y2.append(float(row[1]))

plt.plot(x1, y1, 'r-', label="Obstacles")
plt.plot(x2, y2, 'b-', label="No obstacles")
plt.fill_between([0, 200], [0, 0], [5, 5], color="wheat")
plt.axvline(x=600, color='black')
plt.legend()
plt.title("Heavy traffic")
plt.xlabel("turn")
plt.ylabel("avg velocity")
plt.grid()
plt.savefig("test2/slalom_l.png")
plt.show()
