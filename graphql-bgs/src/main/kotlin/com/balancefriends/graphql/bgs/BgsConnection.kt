package com.balancefriends.graphql.bgs

import graphql.relay.DefaultConnection
import graphql.relay.Edge
import graphql.relay.PageInfo

class BalanceConnection<T>(edges: List<Edge<T>>, pageInfo: PageInfo, val totalCount: Long) :
    DefaultConnection<T>(edges, pageInfo)