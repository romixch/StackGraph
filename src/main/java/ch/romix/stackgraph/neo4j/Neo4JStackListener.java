package ch.romix.stackgraph.neo4j;

import static ch.romix.stackgraph.neo4j.Neo4jConstants.*;

import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import ch.romix.stackgraph.RelationshipTypes;
import ch.romix.stackgraph.collector.StackElement;
import ch.romix.stackgraph.collector.StackListener;
import ch.romix.stackgraph.collector.StackThreadState;

public class Neo4JStackListener implements StackListener {

	private GraphDatabaseService db;

	public Neo4JStackListener(GraphDatabaseService graphDb) {
		this.db = graphDb;
	}

	@Override
	public void onStack(List<StackElement> stack, String threadName, StackThreadState state) {
		try (Transaction tx = db.beginTx()) {
			Node fromMethodNode = null;
			for (StackElement toMethodStack : stack) {
				Node toMethodNode = findOrCreateNode(toMethodStack);
				if (fromMethodNode == null) {
					Long leafCount = (Long) toMethodNode.getProperty(LEAFCOUNT_NODE_PROPERTY);
					leafCount++;
					toMethodNode.setProperty(LEAFCOUNT_NODE_PROPERTY, leafCount);
				} else {
					Iterable<Relationship> relationships = toMethodNode.getRelationships(Direction.INCOMING, RelationshipTypes.calls);
					Relationship calls = null;
					for (Relationship relationship : relationships) {
						if (relationship.getStartNode().equals(fromMethodNode)) {
							calls = relationship;
							Long count = (Long) calls.getProperty(CALLCOUNT_RELATION_PROPERTY);
							calls.setProperty(CALLCOUNT_RELATION_PROPERTY, count++);
							break;
						}
					}
					if (calls == null) {
						calls = toMethodNode.createRelationshipTo(fromMethodNode, RelationshipTypes.calls);
						calls.setProperty(CALLCOUNT_RELATION_PROPERTY, 1L);
					}
				}
				fromMethodNode = toMethodNode;
			}
			tx.success();
		}
	}

	private Node findOrCreateNode(StackElement stackElement) {
		final Node node;
		final Label label = DynamicLabel.label(STACKELEMENT_LABEL);
		ResourceIterable<Node> iterable = db.findNodesByLabelAndProperty(label, STACKELEMENT_NODE_PROPERTY, stackElement.asString());
		if (iterable.iterator().hasNext()) {
			node = iterable.iterator().next();
		} else {
			node = db.createNode(label);
			node.setProperty(STACKELEMENT_NODE_PROPERTY, stackElement.asString());
			node.setProperty(LEAFCOUNT_NODE_PROPERTY, Long.valueOf(0));
		}
		return node;
	}

}
