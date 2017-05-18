Winetasting
===========

Wine Tasting

A large group of friends from the town of Nocillis visit the vineyards of Apan to taste wines. The vineyards produce many fine wines and the friends decide to buy as many as 3 bottles of wine each if they are available to purchase. Unfortunately, the vineyards of Apan have a peculiar restriction that they can not sell more than one bottle of the same wine. So the vineyards come up with the following scheme: They ask each person to write down a list of up to 10 wines that they enjoyed and would be happy buying. With this information, please help the vineyards maximize the number of wines that they can sell to the group of friends.

Input 
A two-column TSV file with the first column containing the ID (just a string) of a person and the second column the ID of the wine that they like. Here are three input data sets of increasing sizes. Please send us solutions even if it runs only on the first file.

https://s3.amazonaws.com/br-user/puzzles/person_wine_3.txt
https://s3.amazonaws.com/br-user/puzzles/person_wine_4.txt.zip
https://s3.amazonaws.com/br-user/puzzles/person_wine_5.txt.zip

Output 
First line contains the number of wine bottles sold in aggregate with your solution. Each subsequent line should be two columns, tab separated. The first column is an ID of a person and the second column should be the ID of the wine that they will buy.

Please check your work. Note that the IDs of the output second column should be unique since a single bottle of wine can not be sold to two people and an ID on the first column can appear at most three times since each person can only buy up to 3 bottles of wine.

Solution
===========
This solution is in Java but the basic algorithm is relatively simple. It mimics PageRank in a way, though of course the problem itself is simple.

Intuitively, a smart vineyard owner would try to sell a bottle with fewer bidders first. Why? Because there is less chance that you can sell this bottle, compared to a bottle with, say 10 bidders.

Starting from this intuition, we need to build up our algorithm. Thankfully I studied a bit of PageRank (still my most favorite algorithm for ranking importance of data so far), so I took the liberty and devised a simpler algorithm for this Wine Tasting problem:
- Consider each wine bottle and each buyer a node in a graph. If a buyer is interested in a bottle, we draw an edge between them. (This is for intuition - the graph is not used in the final algorithm).
- Each bottle is assigned a score of 10.0. 
- The weight of each edge = (weight of bottle)/(# of edges the bottle has)
- The weight of each buyer = SUM(weight of edges)
- The weight of each bottle = the weight of a single edge connecting it

In this manner, bottles with fewer edges (less buyers) will have more weight. Also, a buyer who is interested in bottles with few buyers would be given higher priority than people who "compete" for popular bottles.

Implementation
===========
I first started with HashMap in Java. But HashMap turned out to be rather costly as it uses pointers (64 bit machines), in addition to other Java class' information. Memory consumption was significant even for the first test case (around 300MB memory) so I decided that it was not scalable enough.

I did some research and decided to use SQLite in-memory database for this problem for several reasons:
- Extremely lightweight as SQlite runs native code.
- Faster speed for handing data (in-memory database only)
- SQL is a powerful language and can handle certain transformations needed for this problem
- We can convert to a file-based database if we need to handle larger files.

The final result was positive:
- Memory consumption is significantly reduced (30MB for first test case, 300MB for second test case, around 5GB for the third test case). Luckily I have access to a server with lots of RAM (25GB) so the last case was not a big issue.
- Running time was fast (around 5 minutes for the 2nd case) - unsure about the last case as I don't have time to re-run the test.

After Thoughts
===========
The code can be improved in several ways:
- The SQLite library in use is rather outdated (2.8.2). Faster version is better
- Multithreading definitely can bring down the time. We can multithread most parts of the code as the sellers' ids are in a determinable range (by passing the file once). However, the library I use here does not support multithreading for in-memory database so I decided to abandon the idea. If the data set is even larger, we probably need file-based database instead.
- This can be done on Hadoop easily (without SQLite). Of course the trade-off is running time unless we have a cluster large enough.
