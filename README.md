# CSE-132-HW3

This program performs a semi-naive transitive closure on a graph constructed by SQL databases representing accounts, customers and depositors at a bank. The connections in this graph represent transactions between accounts which may or may not be owned by the same customer. This program uses the JDBC plugin to interface with the databases directly with Java and perform updates.

The final result of this program is a table called influence which stores transactions between customers in the form of FROM, TO tuples.
