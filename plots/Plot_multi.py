import csv
import matplotlib.pyplot as plt

x1 = []
y1 = []

x2 = []
y2 = []

x3 = []
y3 = []

x4 = []
y4 = []

with open('blind_nice.csv', 'rt') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        x1.append(float(row[0]))
        y1.append(float(row[1]))

with open('blind_rude.csv', 'rt') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        x2.append(float(row[0]))
        y2.append(float(row[1]))

with open('see_nice.csv', 'rt') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        x3.append(float(row[0]))
        y3.append(float(row[1]))

with open('see_rude.csv', 'rt') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    for row in csv_reader:
        x4.append(float(row[0]))
        y4.append(float(row[1]))

plt.plot(x1, y1, 'r-', label="Nice, Small Visibility")
plt.plot(x2, y2, 'b-', label="Rude, Small Visibility")
plt.plot(x3, y3, 'g-', label="Nice, Good Visibility")
plt.plot(x4, y4, 'y-', label="Rude, Good Visibility")
plt.legend()
plt.title("Obstacle simulation")
plt.xlabel("turn")
plt.ylabel("avg velocity")
plt.grid()
plt.savefig("test2/slalom.png")
plt.show()
