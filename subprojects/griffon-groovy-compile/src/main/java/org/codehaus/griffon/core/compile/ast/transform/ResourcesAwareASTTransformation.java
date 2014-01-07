/*
 * Copyright 2009-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.griffon.core.compile.ast.transform;

import griffon.transform.ResourcesAware;
import org.codehaus.griffon.core.compile.ResourcesAwareConstants;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codehaus.griffon.core.compile.ast.GriffonASTUtils.injectInterface;

/**
 * Handles generation of code for the {@code @ResourcesAware} annotation.
 *
 * @author Andres Almiray
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ResourcesAwareASTTransformation extends AbstractASTTransformation implements ResourcesAwareConstants {
    private static final Logger LOG = LoggerFactory.getLogger(ResourcesAwareASTTransformation.class);
    private static final ClassNode RESOURCE_HANDLER_CNODE = makeClassSafe(RESOURCE_HANDLER_TYPE);
    private static final ClassNode RESOURCES_AWARE_CNODE = makeClassSafe(ResourcesAware.class);

    /**
     * Convenience method to see if an annotated node is {@code @ResourcesAware}.
     *
     * @param node the node to check
     * @return true if the node is an event publisher
     */
    public static boolean hasResourcesAwareAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (RESOURCES_AWARE_CNODE.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);
        addResourceHandlerIfNeeded(source, (ClassNode) nodes[1]);
    }

    public static void addResourceHandlerIfNeeded(SourceUnit source, ClassNode classNode) {
        if (needsDelegate(classNode, source, METHODS, "ResourcesAware", RESOURCE_HANDLER_TYPE)) {
            LOG.debug("Injecting {} into {}", RESOURCE_HANDLER_TYPE, classNode.getName());
            apply(classNode);
        }
    }

    /**
     * Adds the necessary field and methods to support resource locating.
     *
     * @param declaringClass the class to which we add the support field and methods
     */
    public static void apply(ClassNode declaringClass) {
        injectInterface(declaringClass, RESOURCE_HANDLER_CNODE);
        injectApplication(declaringClass);
        Expression resourceLocator = applicationProperty(declaringClass, RESOURCE_LOCATOR_PROPERTY);
        addDelegateMethods(declaringClass, RESOURCE_HANDLER_CNODE, resourceLocator);
    }
}