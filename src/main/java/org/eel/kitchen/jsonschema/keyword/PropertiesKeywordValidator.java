/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.jsonschema.keyword;

import org.codehaus.jackson.JsonNode;
import org.eel.kitchen.jsonschema.ValidationReport;
import org.eel.kitchen.jsonschema.container.ObjectValidator;
import org.eel.kitchen.jsonschema.context.ValidationContext;
import org.eel.kitchen.jsonschema.syntax.PropertiesSyntaxValidator;
import org.eel.kitchen.jsonschema.syntax.SyntaxValidator;
import org.eel.kitchen.util.CollectionUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>Keyword validator for the {@code properties} and {@code required}
 * keywords (draft sections 5.2 and 5.7).</p>
 *
 * <p>Well, this validator only really validates {@code required}: the syntax
 * of the keyword was already checked by {@link PropertiesSyntaxValidator},
 * so there is no point in checking it again here,
 * and it is up to {@link ObjectValidator} to spawn children validators.</p>
 *
 * @see ObjectValidator
 * @see SyntaxValidator
 */
public final class PropertiesKeywordValidator
    extends KeywordValidator
{
    public PropertiesKeywordValidator()
    {
        super("properties");
    }

    @Override
    public ValidationReport validate(final ValidationContext context,
        final JsonNode instance)
    {
        final ValidationReport report = context.createReport();
        final JsonNode properties = context.getSchemaNode().get(keyword);

        final SortedSet<String> required = new TreeSet<String>();

        final Map<String, JsonNode> map
            = CollectionUtils.toMap(properties.getFields());

        for (final Map.Entry<String, JsonNode> entry: map.entrySet())
            if (entry.getValue().path("required").asBoolean(false))
                required.add(entry.getKey());

        if (required.isEmpty())
            return report;

        final Iterator<String> fields = instance.getFieldNames();

        while (fields.hasNext())
            required.remove(fields.next());

        for (final String missing: required)
            report.addMessage("required property " + missing + " is missing");

        return report;
    }
}
