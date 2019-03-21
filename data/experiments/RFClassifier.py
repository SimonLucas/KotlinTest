from sklearn.model_selection import KFold
from sklearn.metrics import accuracy_score
from sklearn.ensemble import RandomForestClassifier
import numpy as np
from random import shuffle
import matplotlib.pyplot as plt
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC

def load_data():

    path = "data.txt"

    X, y = [], []

    with open(path, 'r') as data_file:

        for line in data_file:

            splitted = line.split(" ")

            X.append([int(splitted[0][1]),
                     int(splitted[1][0]),
                     int(splitted[2][0]),
                     int(splitted[3][0]),
                     int(splitted[4][0]),
                     int(splitted[5][0]),
                     int(splitted[6][0]),
                     int(splitted[7][0]),
                      int(splitted[8][0]),

                      np.sum([int(splitted[0][1]),
                     int(splitted[1][0]),
                     int(splitted[2][0]),
                     int(splitted[3][0]),
                     int(splitted[4][0]),
                     int(splitted[5][0]),
                     int(splitted[6][0]),
                     int(splitted[7][0]),
                      int(splitted[8][0])])/10
                      ])


            y.append(int(splitted[9][0]))

    return X, y


def train_and_evaluate(_X, _y, sample, model):

    indeces = [i for i in range(len(_X))]
    shuffle(indeces)

    X_train = []
    y_train = []

    i = 0
    while i < sample:

        X_train.append(_X[indeces[i]])
        y_train.append(_y[indeces[i]])
        i += 1

    if model == "rf":
        clf = RandomForestClassifier(n_estimators=100, max_depth=10)
    else:
        clf = SVC(C=100, kernel='rbf')

    try:

        clf.fit(X_train, y_train)
    except:
        return 0

    y_pred = clf.predict(_X)

    return accuracy_score(_y, y_pred)


X, y = load_data()
accuracies = []

plt.title("SVC vs RandomForest")
plt.xlabel("samples")
plt.ylabel("accuracy")
accuracies = []

for sample in range(1, 513):

     accuracies.append(train_and_evaluate(X, y, sample, "svm"))

plt.plot(accuracies, label = "svm")

print(accuracies)

accuracies = []

for sample in range(1, 513):

    accuracies.append(train_and_evaluate(X, y, sample, "rf"))

print(accuracies)

plt.plot(accuracies, label = "rf")
plt.legend()
plt.show()
